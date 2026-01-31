package com.restaurante.resturante.controller.security;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin("*")
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

        // 1. Autenticación estándar de Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        User user = (User) authentication.getPrincipal();
        String clientIp = getClientIp(request);

        // 2. Buscamos su acceso (Relación 1 a 1 que definimos)
        // Usamos findFirst() porque bajo tu nueva regla, solo esperamos uno activo.
        UserAccess acceso = userAccessRepository.findByUserIdAndActiveTrue(user.getId())
                .stream()
                .findFirst()
                .orElse(null); // Si es null, podría ser un SuperAdmin sin sede asignada

        String empresaId = (acceso != null) ? acceso.getEmpresa().getId() : null;
        String sucursalId = (acceso != null) ? acceso.getSucursal().getId() : null;

        // 3. Generamos tokens con contexto
        String accessToken = jwtService.generateAccessToken(user, clientIp, empresaId, sucursalId);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

        // 4. Calculamos expiraciones para el DTO
        Date expirationAccessToken = new Date(System.currentTimeMillis() + accessTokenExpiration);
        Date expirationRefreshToken = new Date(System.currentTimeMillis() + refreshTokenExpiration);

        // 5. Devolvemos la respuesta
        // El flag 'requiresSelection' va en false porque ya sabemos su sede
        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                refreshToken,
                expirationAccessToken,
                expirationRefreshToken,
                false,
                List.of()));
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
                List.of());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest requestBody,
            HttpServletRequest request) {

        return refreshTokenService.findByToken(requestBody.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(tokenEntity -> {
                    User user = tokenEntity.getUser();
                    String clientIp = getClientIp(request);

                    // IMPORTANTE: Recuperamos el acceso para mantener el contexto en el nuevo token
                    UserAccess acceso = userAccessRepository.findByUserIdAndActiveTrue(user.getId())
                            .stream().findFirst().orElse(null);

                    String empId = (acceso != null) ? acceso.getEmpresa().getId() : null;
                    String sucId = (acceso != null) ? acceso.getSucursal().getId() : null;

                    String newAccessToken = jwtService.generateAccessToken(user, clientIp, empId, sucId);

                    // Rotación de Refresh Token (opcional pero recomendado)
                    refreshTokenRepository.delete(tokenEntity);
                    String newRefreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

                    return ResponseEntity.ok(new AuthResponse(
                            newAccessToken,
                            newRefreshToken,
                            new Date(System.currentTimeMillis() + accessTokenExpiration),
                            new Date(System.currentTimeMillis() + refreshTokenExpiration),
                            false,
                            List.of()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestBody.refreshToken(), "Token inválido"));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0];
    }
}
