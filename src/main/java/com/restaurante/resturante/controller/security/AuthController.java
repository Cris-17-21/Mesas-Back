package com.restaurante.resturante.controller.security;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalSelectionRequest;
import com.restaurante.resturante.dto.security.AuthResponse;
import com.restaurante.resturante.dto.security.LoginRequest;
import com.restaurante.resturante.dto.security.RefreshTokenRequest;
import com.restaurante.resturante.exception.TokenRefreshException;
import com.restaurante.resturante.mapper.maestros.SucursalDtoMapper;
import com.restaurante.resturante.repository.security.RefreshTokenRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.service.security.JwtService;
import com.restaurante.resturante.service.security.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccessRepository userAccessRepository;
    private final SucursalDtoMapper sucursalDtoMapper;

    @Value("${security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        User user = (User) authentication.getPrincipal();
        String clientIp = getClientIp(request);

        List<UserAccess> accesos = userAccessRepository.findByUserIdAndActiveTrue(user.getId());

        if (accesos.isEmpty()) {
            return ResponseEntity.ok(createAuthResponse(user, clientIp, null, null));
        }

        // CASO A: Múltiples sucursales (Requiere selección)
        if (accesos.size() > 1) {
            List<SucursalDto> sucursales = accesos.stream()
                    .map(acc -> sucursalDtoMapper.toDto(acc.getSucursal()))
                    .toList();

            return ResponseEntity.ok(new AuthResponse(
                    null, null, null, null,
                    true,
                    sucursales));
        }

        // CASO B: Acceso directo (1 sucursal o ninguna si es SuperAdmin)
        String empresaId = null;
        String sucursalId = null;
        if (accesos.size() == 1) {
            UserAccess acc = accesos.get(0);
            empresaId = acc.getEmpresa().getId();
            sucursalId = acc.getSucursal().getId();
        }

        String accessToken = jwtService.generateAccessToken(user, clientIp, empresaId, sucursalId);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

        // UserDto userDto = userDtoMapper.toUserDto(user);

        Date expirationAccessToken = new Date(System.currentTimeMillis() + accessTokenExpiration);
        Date expirationRefreshToken = new Date(System.currentTimeMillis() + refreshTokenExpiration);

        // return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken,
        // expirationAccessToken, expirationRefreshToken, userDto));
        return ResponseEntity
                .ok(new AuthResponse(accessToken, refreshToken, expirationAccessToken, expirationRefreshToken, false,
                        java.util.Collections.emptyList()));
    }

    @PostMapping("/select-branch")
    public ResponseEntity<AuthResponse> selectBranch(@RequestBody SucursalSelectionRequest selection,
            HttpServletRequest request) {
        // Validamos que el acceso sea real y pertenezca al usuario logueado
        UserAccess acceso = userAccessRepository
                .findByUserIdAndSucursalId(selection.userId(), selection.sucursalId())
                .orElseThrow(() -> new AccessDeniedException("No tienes permiso para acceder a esta sede"));

        return ResponseEntity.ok(createAuthResponse(acceso.getUser(), getClientIp(request),
                acceso.getEmpresa().getId(), acceso.getSucursal().getId()));
    }

    // El método de apoyo permanece igual para centralizar la creación de tokens
    private AuthResponse createAuthResponse(User user, String ip, String empresaId, String sucursalId) {
        String accessToken = jwtService.generateAccessToken(user, ip, empresaId, sucursalId);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

        return new AuthResponse(
                accessToken,
                refreshToken,
                new java.util.Date(System.currentTimeMillis() + accessTokenExpiration),
                new java.util.Date(System.currentTimeMillis() + refreshTokenExpiration),
                false,
                List.of()
            );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest requestBody,
            HttpServletRequest request) {
        String requestRefreshToken = requestBody.refreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    refreshTokenRepository.delete(refreshToken);
                    String newRefreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();
                    String clientIp = getClientIp(request);
                    String accessToken = jwtService.generateAccessToken(user, clientIp, null, null);

                    // UserDto userDto = userDtoMapper.toUserDto(user);

                    Date expirationAccessToken = new Date(System.currentTimeMillis() + accessTokenExpiration);
                    Date expirationRefreshToken = new Date(System.currentTimeMillis() + refreshTokenExpiration);

                    // return ResponseEntity.ok(new AuthResponse(accessToken, newRefreshToken,
                    // expirationAccessToken, expirationRefreshToken, userDto));
                    return ResponseEntity.ok(new AuthResponse(accessToken, newRefreshToken, expirationAccessToken,
                            expirationRefreshToken, false, java.util.Collections.emptyList()));

                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token no encontrado..."));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0];
    }
}
