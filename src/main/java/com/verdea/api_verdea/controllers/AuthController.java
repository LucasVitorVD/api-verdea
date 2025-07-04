package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.userDto.*;
import com.verdea.api_verdea.entities.ApiError;
import com.verdea.api_verdea.services.authentication.AuthenticationService;
import com.verdea.api_verdea.services.authentication.CookieService;
import com.verdea.api_verdea.services.resetPassword.PasswordResetService;
import com.verdea.api_verdea.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints for managing authentication")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class AuthController {
    private final AuthenticationService authenticationService;
    private final CookieService cookieService;
    private final PasswordResetService service;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid data or email already in use", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserRequestDTO request) {
        UserResponseDTO registeredUser = userService.registerUser(request);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticate a user and set JWT tokens in cookies",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<String> authenticate(@RequestBody UserRequestDTO request, @Parameter(hidden = true) HttpServletResponse response) {
        LoginResponse tokens = authenticationService.authenticate(request);

        cookieService.addAccessTokenCookie(response, tokens.accessToken());
        cookieService.addRefreshTokenCookie(response, tokens.refreshToken());

        return ResponseEntity.ok("Usuário autenticado!");
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh JWT tokens",
            description = "Generate new access and refresh tokens using the refresh token in the cookie",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing refresh token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<LoginResponse> refreshToken(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
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
    @Operation(
            summary = "Logout user",
            description = "Revoke refresh token and delete cookies",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User logged out successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) {
        UUID refreshToken = cookieService.extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            authenticationService.revokeRefreshToken(refreshToken);
        }

        cookieService.deleteAccessTokenCookie(response);
        cookieService.deleteRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Send password reset email",
            description = "Send an email with a password reset link",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ForgotPasswordRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reset email sent successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<String> sendLink(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        service.sendResetPasswordEmail(forgotPasswordRequest.email());

        return ResponseEntity.ok("E-mail enviado!");
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset user password",
            description = "Update the user's password using the token sent via email",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResetPasswordRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<String> reset(@RequestBody ResetPasswordRequest request) {
        service.resetPassword(request.token(), request.newPassword());

        return ResponseEntity.ok("Senha redefinida com sucesso");
    }
}