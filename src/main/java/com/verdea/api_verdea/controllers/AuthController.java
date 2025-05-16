package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.dtos.userDto.LoginResponse;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.services.authentication.AuthenticationService;
import com.verdea.api_verdea.services.authentication.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody UserRequestDTO request, HttpServletResponse response) {
        LoginResponse tokens = authenticationService.authenticate(request);

        cookieService.addAccessTokenCookie(response, tokens.accessToken());
        cookieService.addRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok("Usuário autenticado!");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        UUID refreshToken = cookieService.extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            LoginResponse loginResponse = authenticationService.refreshToken(refreshToken);

            cookieService.addAccessTokenCookie(response, loginResponse.accessToken());
            cookieService.addRefreshTokenCookie(response, loginResponse.refreshToken());

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            cookieService.deleteAccessTokenCookie(response);
            cookieService.deleteRefreshTokenCookie(response);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        UUID refreshToken = cookieService.extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            authenticationService.revokeRefreshToken(refreshToken);
        }

        // Remove os cookies
        cookieService.deleteAccessTokenCookie(response);
        cookieService.deleteRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }
}