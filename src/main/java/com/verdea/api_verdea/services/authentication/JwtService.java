package com.verdea.api_verdea.services.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {
    private final String issuer;
    private final Duration ttl;

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder, @Value("${jwt.issuer}") String issuer, @Value("${jwt.ttl}") Duration ttl) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    public String generateToken(String email) {
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(email)
                .expiresAt(Instant.now().plus(ttl))
                .build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));

        return jwt.getTokenValue();
    }
}
