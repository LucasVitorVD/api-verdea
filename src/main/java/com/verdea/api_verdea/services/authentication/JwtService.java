package com.verdea.api_verdea.services.authentication;

import com.verdea.api_verdea.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.ttl}") Duration accessTokenTtl,
            @Value("${jwt.refresh-token.ttl}") Duration refreshTokenTtl
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public String generateAccessToken(User user) {
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            Jwt decoded = jwtDecoder.decode(token);
            if (!decoded.getSubject().equals(expectedEmail)) return false;
            assert decoded.getExpiresAt() != null;
            return decoded.getExpiresAt().isAfter(Instant.now());
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return jwtDecoder.decode(token).getSubject();
    }
}
