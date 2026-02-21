package com.jd.apvd.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret:MyVerySecureSecretKeyForJWTTokenGenerationAndValidationPurpose123456789}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long jwtExpirationMs;
    
    public String generateToken(String userId, String userEmail, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEmail);
        claims.put("role", role);
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject();
    }
    
    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }
    
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }
    
    public boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
    
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
    
    public long getTokenExpirationTime(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
    
    private Claims getClaims(String token) {
        try {
            SecretKey key = getSigningKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw new RuntimeException("Cannot extract claims from token", e);
        }
    }
    
    private SecretKey getSigningKey() {
        // Ensure the secret is at least 512 bits (64 bytes) for HS512
        String secret = jwtSecret;
        if (secret.length() < 64) {
            // If secret is too short, pad it to 64 characters
            secret = String.format("%-64s", secret).replace(' ', '0');
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
