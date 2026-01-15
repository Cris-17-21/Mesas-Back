package com.restaurante.resturante.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private static final String CLAIM_IP_ADDRESS = "ip";
    private static final String CLAIM_AUTHORITIES = "authorities";

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-token.expiration}")
    private long accessTokenExpiration;
    
    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateAccessToken(UserDetails userDetails, String ipAddress) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(CLAIM_IP_ADDRESS, ipAddress);
        
        extraClaims.put(CLAIM_AUTHORITIES, userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return buildToken(extraClaims, userDetails, this.accessTokenExpiration);
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, this.refreshTokenExpiration);
    }
    
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.signingKey)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, String clientIpAddress) {
        try {
            final String username = extractUsername(token)
                    .orElseThrow(() -> new MalformedJwtException("El token no contiene username"));
            final String tokenIpAddress = extractIpAddress(token)
                    .orElseThrow(() -> new MalformedJwtException("El token no contiene dirección IP"));
            
            return username.equals(userDetails.getUsername()) &&
                   !isTokenExpired(token) &&
                   clientIpAddress.equals(tokenIpAddress);
        } catch (Exception e) {
            logger.error("La validación del token falló: {}", e.getMessage());
            return false;
        }
    }

    public Optional<String> extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public Optional<String> extractIpAddress(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_IP_ADDRESS, String.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).map(date -> date.before(new Date())).orElse(false);
    }

    private Optional<Date> extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> Optional<T> extractClaim(String token, Function<Claims, T> claimsResolver) {
        return extractAllClaims(token).map(claimsResolver);
    }


    private Optional<Claims> extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(this.signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            logger.warn("El token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Token JWT no soportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT malformado: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("Firma del token JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Argumento del token JWT inválido: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
