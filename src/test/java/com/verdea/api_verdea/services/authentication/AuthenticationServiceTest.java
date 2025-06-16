package com.verdea.api_verdea.services.authentication;

import com.verdea.api_verdea.dtos.userDto.LoginResponse;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.entities.RefreshToken;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.InvalidRefreshTokenException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.RefreshTokenRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserRequestDTO userRequestDTO;

    private User user;

    @BeforeEach
    void setup() {
        String email = "teste@gmail.com";
        String password = "password";
        userRequestDTO = new UserRequestDTO(email, password);

        user = new User();
        user.setEmail(email);
        user.setPassword(password);
    }

    @Test
    @DisplayName("Should authenticate user and return tokens")
    void authenticateCase1() {
        String accessToken = "mocked-access-token";
        UUID refreshTokenId = UUID.randomUUID();

        Authentication mockAuth = mock(Authentication.class);

        when(mockAuth.getName()).thenReturn(userRequestDTO.email());
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtService.generateToken(userRequestDTO.email())).thenReturn(accessToken);

        User user = new User();
        user.setEmail(userRequestDTO.email());
        when(userRepository.findByEmail(userRequestDTO.email())).thenReturn(Optional.of(user));

        doAnswer(invocationOnMock -> {
            RefreshToken token = invocationOnMock.getArgument(0);
            token.setId(refreshTokenId);
            return token;
        }).when(refreshTokenRepository).save(any(RefreshToken.class));

        LoginResponse response = authenticationService.authenticate(userRequestDTO);

        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshTokenId, response.refreshToken());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void authenticateCase2() {
        String email = "teste@gmail.com";
        String password = "password";
        UserRequestDTO userRequestDTO = new UserRequestDTO(email, password);

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(email);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.authenticate(userRequestDTO);
        });
    }

    @Test
    @DisplayName("Should return new access token when refresh token is valid")
    void refreshTokenCase1() {
        UUID refreshTokenId = UUID.randomUUID();
        String email = "teste@gmail.com";
        String newAccessToken = "new-access-token";

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setId(refreshTokenId);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));

        when(refreshTokenRepository.findByIdAndExpiresAtAfter(eq(refreshTokenId), any(Instant.class))).thenReturn(Optional.of(refreshTokenEntity));
        when(jwtService.generateToken(email)).thenReturn(newAccessToken);

        LoginResponse response = authenticationService.refreshToken(refreshTokenId);

        assertEquals(newAccessToken, response.accessToken());
        assertEquals(refreshTokenId, response.refreshToken());

        verify(refreshTokenRepository, times(1)).findByIdAndExpiresAtAfter(eq(refreshTokenId), any(Instant.class));
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid or expired")
    void revokeRefreshTokenCase2() {
        UUID refreshTokenId = UUID.randomUUID();

        when(refreshTokenRepository.findByIdAndExpiresAtAfter(eq(refreshTokenId), any())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> {
            authenticationService.refreshToken(refreshTokenId);
        });

        verify(jwtService, never()).generateToken(any());
    }
}