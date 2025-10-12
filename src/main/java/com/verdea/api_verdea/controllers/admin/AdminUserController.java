package com.verdea.api_verdea.controllers.admin;

import com.verdea.api_verdea.dtos.userDto.UpdateUserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.services.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Admin endpoints for managing users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List all users")
    public List<UserResponseDTO> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return adminUserService.getUserById(id);
    }

    @PostMapping
    @Operation(summary = "Create new user")
    public UserResponseDTO createUser(@RequestBody UserRequestDTO dto) {
        return adminUserService.createUser(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by ID")
    public UserResponseDTO updateUser(@PathVariable Long id, @RequestBody UpdateUserRequestDTO dto) {
        return adminUserService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}