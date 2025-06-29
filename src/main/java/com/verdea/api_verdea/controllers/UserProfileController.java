package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.services.authentication.AuthenticationService;
import com.verdea.api_verdea.services.authentication.CookieService;
import com.verdea.api_verdea.services.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final CookieService cookieService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserProfile(Authentication authentication) {
        String email = authentication.getName();

        UserResponseDTO savedUser = userService.getUserByEmail(email);

        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteOwnUser(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String email = authentication.getName();
        UserResponseDTO user = userService.getUserByEmail(email);

        userService.deleteUser(user.id());

        UUID refreshToken = cookieService.extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            authenticationService.revokeRefreshToken(refreshToken);
        }

        cookieService.deleteAccessTokenCookie(response);
        cookieService.deleteRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }
}
