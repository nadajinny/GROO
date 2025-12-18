package com.groo.security;

import com.groo.config.JwtProperties;
import com.groo.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey key;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(JwtProperties properties, UserDetailsService userDetailsService) {
        this.properties = properties;
        this.userDetailsService = userDetailsService;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant expiry = Instant.now().plus(properties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiry))
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant expiry = Instant.now().plus(properties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiry))
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    public Instant getRefreshTokenExpiryInstant() {
        return Instant.now().plus(properties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
    }

    public long getAccessTokenExpirySeconds() {
        return properties.getAccessTokenExpirationMinutes() * 60;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        var userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
