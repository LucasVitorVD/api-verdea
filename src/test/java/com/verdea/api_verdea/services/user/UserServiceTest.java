package com.verdea.api_verdea.services.user;

import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.EmailAlreadyInUseException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.mappers.UserMapper;
import com.verdea.api_verdea.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO expectedResponse;

    @BeforeEach
    void setup() {
        user = User.builder()
                .email("test@gmail.com")
                .password("encodedPassword")
                .build();

        userRequestDTO = new UserRequestDTO("test@gmail.com", "plainPassword");

        expectedResponse = new UserResponseDTO(1L, "test@gmail.com", LocalDateTime.now());
    }

    @Test
    @DisplayName("Should register new user successfully")
    void registerUserCase1() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(userRequestDTO.password())).thenReturn("encodedPassword");
        
        when(userRepository.save(user)).thenAnswer(invocation -> {
            user.setId(1L);

            return user;
        });

        when(userMapper.entityToResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponseDTO result = userService.registerUser(userRequestDTO);

        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.email(), result.email());

        verify(userRepository).existsByEmail(userRequestDTO.email());
        verify(passwordEncoder).encode(userRequestDTO.password());
        verify(userRepository).save(any(User.class));
        verify(userMapper).entityToResponse(any(User.class));

        assertEquals(userRequestDTO.email(), result.email());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyInUseException if user already exists")
    void registerUserCase2() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> userService.registerUser(userRequestDTO));

        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).entityToResponse(any(User.class));
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void getUserByEmailCase1() {
        when(userRepository.findByEmail(userRequestDTO.email())).thenReturn(Optional.of(user));
        when(userMapper.entityToResponse(user)).thenReturn(expectedResponse);

        UserResponseDTO result = userService.getUserByEmail(userRequestDTO.email());

        assertEquals(result.email(), expectedResponse.email());
        assertNotNull(result.id());

        verify(userMapper, times(1)).entityToResponse(any(User.class));
    }

    @Test
    @DisplayName("Should throw an UserNotFoundException if user does not exists")
    void getUserByEmailCase2() {
        when(userRepository.findByEmail(userRequestDTO.email())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(userRequestDTO.email()));

        verify(userMapper, never()).entityToResponse(any(User.class));
    }
}