package com.restaurante.resturante.controller.security;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.domain.security.User;
import com.restaurante.resturante.dto.security.AuthResponse;
import com.restaurante.resturante.dto.security.LoginRequest;
import com.restaurante.resturante.dto.security.RefreshTokenRequest;
import com.restaurante.resturante.exception.TokenRefreshException;
import com.restaurante.resturante.repository.security.RefreshTokenRepository;
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
    
    @Value("${security.jwt.access-token.expiration}")
    private long accessTokenExpiration;
    
    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        User user = (User) authentication.getPrincipal();
        String clientIp = getClientIp(request);

        String accessToken = jwtService.generateAccessToken(user, clientIp);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();

        //UserDto userDto = userDtoMapper.toUserDto(user);

        Date expirationAccessToken = new Date(System.currentTimeMillis() + accessTokenExpiration);
        Date expirationRefreshToken = new Date(System.currentTimeMillis() + refreshTokenExpiration);

        //return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, expirationAccessToken, expirationRefreshToken, userDto));
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, expirationAccessToken, expirationRefreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest requestBody, HttpServletRequest request) {
        String requestRefreshToken = requestBody.refreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    User user = refreshToken.getUser();
                    refreshTokenRepository.delete(refreshToken);
                    String newRefreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getToken();
                    String clientIp = getClientIp(request);
                    String accessToken = jwtService.generateAccessToken(user, clientIp);

                    //UserDto userDto = userDtoMapper.toUserDto(user);

                    Date expirationAccessToken = new Date(System.currentTimeMillis() + accessTokenExpiration);
                    Date expirationRefreshToken = new Date(System.currentTimeMillis() + refreshTokenExpiration);

                    //return ResponseEntity.ok(new AuthResponse(accessToken, newRefreshToken, expirationAccessToken, expirationRefreshToken, userDto));
                    return ResponseEntity.ok(new AuthResponse(accessToken, newRefreshToken, expirationAccessToken, expirationRefreshToken));

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
