package com.toplms.core.security;

import com.toplms.config.AppHostProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    private final String SECRET_KEY = "toplms_secret_key";
    private final String TOKEN_PREFIX = "Bearer ";
    private final String HEADER_STRING = "Authorization";
    private final long EXPIRATION_TIME = 86400000; // 24 hours
    private final long REFRESH_TIME = 600000; //

    private AppHostProperties appHostProperties;
    JwtProvider(AppHostProperties appHostProperties) {
        this.appHostProperties = appHostProperties;
    }
    private SecretKey getSigningKey() {
        String secretKey = appHostProperties.getSecretKey();
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT Secret Key configuration is missing from AppHostProperties!");
        }

        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role, String tenantId) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("tenantId", tenantId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }
    public boolean isTokenValid(String token, String currentTenantId) {
        try {
            Claims claims = parseToken(token);
            boolean isExpired = claims.getExpiration().before(new Date());
            String tokenTenantId = claims.get("tenantId", String.class);

            // Critical SaaS Protection: Token must not be expired AND must match the current subdomain
            return !isExpired && currentTenantId.equals(tokenTenantId);
        } catch (Exception e) {
            return false;
        }
    }
}
