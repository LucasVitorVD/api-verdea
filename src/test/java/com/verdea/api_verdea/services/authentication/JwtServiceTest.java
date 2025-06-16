package com.verdea.api_verdea.services.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtEncoderParameters jwtParams;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService(jwtEncoder, "issuer", Duration.ofDays(1));
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void generateTokenCase1() {
        when(jwtEncoder.encode(any())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("fake-jwt-token");

        String token = jwtService.generateToken("test@email.com");

        assertEquals("fake-jwt-token", token);
        verify(jwtEncoder, times(1)).encode(any());
    }
}