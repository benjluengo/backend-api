package com.huertohogar.backend_api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${jwt.secret:defaultSecretKey123456789012345678901234567890}")
    private String secretKey;
    
    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Error al generar la clave de firma: {}", e.getMessage());
            throw new RuntimeException("Error al generar la clave de firma", e);
        }
    }
    
    public String extractUserId(String token) {
        if (token == null) {
            logger.warn("Token nulo proporcionado para extracción de userId");
            return null;
        }

        try {
            return extractClaim(token, claims -> claims.get("userId", String.class));
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            logger.error("Error al procesar el token: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error inesperado al procesar el token: {}", e.getMessage());
            return null;
        }
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error al extraer claims del token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        if (token == null) {
            logger.warn("Token nulo proporcionado para validación");
            return false;
        }

        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expirado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            logger.error("Token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error inesperado al validar el token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            logger.warn("Token ya expirado: {}", e.getMessage());
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar la expiración del token: {}", e.getMessage());
            return true;
        }
    }

    public String generateToken(String userId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + 86400000); // 24 hours

            return Jwts.builder()
                    .setSubject("user")
                    .claim("userId", userId)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            logger.error("Error al generar el token: {}", e.getMessage());
            throw new RuntimeException("Error al generar el token", e);
        }
    }
}
