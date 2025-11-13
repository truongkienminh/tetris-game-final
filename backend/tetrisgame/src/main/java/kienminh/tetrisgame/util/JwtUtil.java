package kienminh.tetrisgame.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


import java.security.Key;


@Component
public class JwtUtil {

    private final String secretKey;
    private final long expirationMillis;

    public JwtUtil(@Value("${jwt.secret:super_secret_tetris_key_123456789_super_secure}") String secretKey,
                   @Value("${jwt.expiration:86400000}") long expirationMillis) {
        this.secretKey = secretKey;
        this.expirationMillis = expirationMillis;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ✅ Tạo token mới
    public String generateToken(String username) {
        Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Lấy username từ token
    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    // ✅ Xác thực token hợp lệ
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Parse token và lấy claims
    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
}
