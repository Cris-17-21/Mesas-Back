package com.restaurante.resturante.controller.security;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.domain.maestros.Sucursal;
import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.domain.security.UserAccess;
import com.restaurante.resturante.dto.maestro.SucursalDto;
import com.restaurante.resturante.dto.maestro.SucursalSelectionRequest;
import com.restaurante.resturante.dto.security.AuthResponse;
import com.restaurante.resturante.dto.security.LoginRequest;
import com.restaurante.resturante.dto.security.RefreshTokenRequest;
import com.restaurante.resturante.exception.TokenRefreshException;
import com.restaurante.resturante.mapper.maestros.SucursalDtoMapper;
import com.restaurante.resturante.repository.maestro.SucursalRepository;
import com.restaurante.resturante.repository.security.RefreshTokenRepository;
import com.restaurante.resturante.repository.security.UserAccessRepository;
import com.restaurante.resturante.service.maestros.ISucursalService;
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
        private final ISucursalService sucursalService;
        private final UserAccessRepository userAccessRepository;
        private final SucursalDtoMapper sucursalDtoMapper;
        private final SucursalRepository sucursalRepository;

        @Value("${security.jwt.access-token.expiration}")
        private long accessTokenExpiration;

        @Value("${security.jwt.refresh-token.expiration}")
        private long refreshTokenExpiration;

        @PostMapping("/login")
        @Transactional
        public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest,
                        HttpServletRequest request) {

                // 1. Autenticación
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginRequest.username(),
                                                loginRequest.password()));

                User user = (User) authentication.getPrincipal();
                String clientIp = getClientIp(request);

                // 2. Verificar roles antes de buscar en UserAccess
                boolean isSuperAdmin = user.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
                boolean isAdmin = user.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ADMIN_RESTAURANTE"));

                // SI ES SUPERADMIN, entra directo sin buscar en UserAccess
                if (isSuperAdmin) {
                        return ResponseEntity.ok(createAuthResponse(user, clientIp, null, null, false, List.of()));
                }

                // 3. Para ADMIN y otros, buscamos su acceso base
                UserAccess accesoBase = userAccessRepository.findByUserIdAndActiveTrue(user.getId())
                                .stream().findFirst().orElse(null);

                // Solo lanzamos error si NO es SuperAdmin y NO tiene acceso
                if (accesoBase == null) {
                        throw new AccessDeniedException("Usuario sin acceso configurado");
                }

                String empresaId = accesoBase.getEmpresa().getId();
                String sucursalId = null;
                boolean requiresSelection = false;
                List<SucursalDto> sucursalesDisponibles = List.of();

                if (isAdmin) {
                        // ADMIN: Buscamos las sucursales de su empresa
                        List<Sucursal> sucursales = sucursalRepository.findByEmpresaId(empresaId);

                        if (sucursales.size() > 1) {
                                requiresSelection = true;
                                sucursalesDisponibles = sucursales.stream()
                                                .map(sucursalDtoMapper::toDto).toList();
                        } else if (sucursales.size() == 1) {
                                sucursalId = sucursales.get(0).getId();
                        }
                } else {
                        // USUARIO NORMAL
                        if (accesoBase.getSucursal() != null) {
                                sucursalId = accesoBase.getSucursal().getId();
                        }
                }

                return ResponseEntity.ok(createAuthResponse(user, clientIp, empresaId, sucursalId,
                                requiresSelection, sucursalesDisponibles));
        }

        @PostMapping("/select-branch")
        @Transactional // Recomendado para operaciones de acceso
        public ResponseEntity<AuthResponse> selectBranch(@RequestBody SucursalSelectionRequest selection,
                        HttpServletRequest request) {

                // 1. Buscamos la sucursal para saber a qué empresa pertenece
                Sucursal sucursal = sucursalRepository.findById(selection.sucursalId())
                                .orElseThrow(() -> new AccessDeniedException("Sucursal no encontrada"));

                // 2. Buscamos al usuario para verificar su rol
                // Nota: Uso el repositorio de sucursalService o userRepository si lo tienes
                // inyectado
                // En tu caso, podemos sacarlo del UserAccess o inyectar el UserRepository
                UserAccess accesoBase = userAccessRepository.findByUserIdAndActiveTrue(
                                userAccessRepository.findByUserUsername(selection.userId()).get(0).getUser().getId())
                                .stream().findFirst()
                                .orElseThrow(() -> new AccessDeniedException("Usuario sin acceso"));

                User user = accesoBase.getUser();

                // 3. LÓGICA DINÁMICA
                boolean isAdmin = user.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ADMIN_RESTAURANTE"));
                boolean isSuperAdmin = user.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

                if (isAdmin || isSuperAdmin) {
                        // Si es Admin, validamos que la sucursal pertenezca a la misma empresa que su
                        // acceso base
                        if (!sucursal.getEmpresa().getId().equals(accesoBase.getEmpresa().getId())) {
                                throw new AccessDeniedException(
                                                "No tienes permiso para acceder a una sede de otra empresa");
                        }
                        // El Admin pasa sin necesidad de tener un registro en UserAccess para esta
                        // sucursal específica
                } else {
                        // Si NO es Admin, validamos que tenga el registro físico en UserAccess
                        userAccessRepository.findByUserUsernameAndSucursalId(selection.userId(), selection.sucursalId())
                                        .orElseThrow(() -> new AccessDeniedException(
                                                        "No tienes permiso para acceder a esta sede"));
                }

                // 4. Retornamos el token final
                return ResponseEntity.ok(createAuthResponse(
                                user,
                                getClientIp(request),
                                sucursal.getEmpresa().getId(),
                                sucursal.getId(),
                                false,
                                List.of()));
        }

        // El método de apoyo permanece igual para centralizar la creación de tokens
        private AuthResponse createAuthResponse(User user, String ip, String empresaId, String sucursalId,
                        boolean requiresSelection, List<SucursalDto> sucursales) {
                String accessToken = jwtService.generateAccessToken(user, ip, empresaId, sucursalId);
                String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

                return new AuthResponse(
                                accessToken,
                                refreshToken,
                                new java.util.Date(System.currentTimeMillis() + accessTokenExpiration),
                                new java.util.Date(System.currentTimeMillis() + refreshTokenExpiration),
                                requiresSelection,
                                sucursales);
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest requestBody,
                        HttpServletRequest request) {
                return refreshTokenService.findByToken(requestBody.refreshToken())
                                .map(refreshTokenService::verifyExpiration)
                                .map(tokenEntity -> {
                                        User user = tokenEntity.getUser();

                                        // 1. Verificamos si es SuperAdmin
                                        boolean isSuperAdmin = user.getAuthorities().stream()
                                                        .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

                                        if (isSuperAdmin) {
                                                // SuperAdmin: Generamos token sin empresa ni sucursal
                                                return ResponseEntity.ok(createAuthResponse(user, getClientIp(request),
                                                                null, null, false, List.of()));
                                        }

                                        // 2. Para los demás (Admin/User), buscamos su acceso activo
                                        UserAccess acceso = userAccessRepository.findByUserIdAndActiveTrue(user.getId())
                                                        .stream().findFirst().orElse(null);

                                        String empId = (acceso != null) ? acceso.getEmpresa().getId() : null;
                                        String sucId = (acceso != null) ? acceso.getSucursal().getId() : null;

                                        return ResponseEntity.ok(createAuthResponse(user, getClientIp(request), empId,
                                                        sucId, false, List.of()));
                                })
                                .orElseThrow(() -> new TokenRefreshException(requestBody.refreshToken(),
                                                "Token inválido"));
        }

        private String getClientIp(HttpServletRequest request) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getRemoteAddr();
                }
                return ip.split(",")[0];
        }
}
