package com.verdea.api_verdea.controllers;

import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.mappers.UserMapper;
import com.verdea.api_verdea.services.authentication.CookieService;
import com.verdea.api_verdea.services.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    @WithMockUser(username = "teste@gmail.com")
    @DisplayName("Should get user details successfully")
    void getUserProfileCase1() throws Exception {
        String email = "teste@gmail.com";
        UserResponseDTO response = new UserResponseDTO(1L, email, null);

        when(userService.getUserByEmail(email)).thenReturn(response);

        mockMvc.perform(get("/api/user/me")
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.email").value(response.email())
                );
    }

    @Test
    @WithMockUser(username = "teste@gmail.com")
    @DisplayName("Should throw user not found exception if user not exists")
    void getUserProfileCase2() throws Exception {
        String email = "teste@gmail.com";

        when(userService.getUserByEmail(email)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/me")
                        .with(csrf()))
                .andExpectAll(
                        status().isNotFound()
                );
    }
}