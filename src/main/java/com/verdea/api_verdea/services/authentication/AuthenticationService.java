package com.verdea.api_verdea.services.authentication;

import com.verdea.api_verdea.dtos.userDto.LoginResponse;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.entities.RefreshToken;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.InvalidRefreshTokenException;
import com.verdea.api_verdea.repositories.RefreshTokenRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Duration refreshTokenTtl = Duration.ofDays(7);

    public LoginResponse authenticate(UserRequestDTO requestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.email(), requestDTO.password())
        );

        String userEmail = authentication.getName();

        var accessToken = jwtService.generateToken(userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário com o email: " + userEmail + " não encontrado"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenTtl));

        refreshTokenRepository.save(refreshToken);

        return new LoginResponse(accessToken, refreshToken.getId());
    }

    public LoginResponse refreshToken(UUID refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByIdAndExpiresAtAfter(refreshToken, Instant.now())
                .orElseThrow(() -> new InvalidRefreshTokenException("Token inválido ou expirado"));

        var newAccessToken = jwtService.generateToken(refreshTokenEntity.getUser().getEmail());

        return new LoginResponse(newAccessToken, refreshToken);
    }

    public void revokeRefreshToken(UUID refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
    }
}