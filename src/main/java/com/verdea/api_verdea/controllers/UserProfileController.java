package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.mappers.UserMapper;
import com.verdea.api_verdea.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUserProfile(Authentication authentication) {
        String email = authentication.getName();

        UserResponseDTO savedUser = userService.getUserByEmail(email);

        return ResponseEntity.ok(savedUser);
    }
}
