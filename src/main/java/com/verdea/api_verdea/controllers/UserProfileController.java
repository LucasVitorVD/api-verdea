package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.config.SecurityConfig;
import com.verdea.api_verdea.dtos.userDto.UpdateUserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.services.authentication.AuthenticationService;
import com.verdea.api_verdea.services.authentication.CookieService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for managing user profile")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class UserProfileController {

    private final UserService userService;
    private final CookieService cookieService;
    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    @Operation(
            summary = "get user profile data",
            description = "method to get all authenticated user profile data",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<UserResponseDTO> getUserProfile(@Parameter(hidden = true) Authentication authentication) {
        String email = authentication.getName();

        UserResponseDTO savedUser = userService.getUserByEmail(email);

        return ResponseEntity.ok(savedUser);
    }

    @PatchMapping("/update")
    @Operation(
            summary = "Update authenticated user profile",
            description = "Update profile data of the authenticated user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "422", description = "Unprocessable entity - email already in use"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid UpdateUserRequestDTO dto
    ) {
        String currentEmail = authentication.getName();

        UserResponseDTO updatedUser = userService.updateUserInfo(currentEmail, dto);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Delete authenticated user account",
            description = "Deletes the currently authenticated user and invalidates associated tokens",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Void> deleteOwnUser(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
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
