package com.verdea.api_verdea.services.user;

import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.mappers.UserMapper;
import com.verdea.api_verdea.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                .id(1L)
                .email("test@gmail.com")
                .password("encodedPassword")
                .build();

        // DTO de entrada
        userRequestDTO = new UserRequestDTO("test@gmail.com", "plainPassword");

        // DTO de resposta esperado
        expectedResponse = new UserResponseDTO(1L, "test@gmail.com", LocalDateTime.now());
    }

    @Test
    @DisplayName("Should register new user successfully")
    void registerUserCase1() {
        when(userRepository.existsByEmail(userRequestDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(userRequestDTO.password())).thenReturn("encodedPassword");

        // Captura o objeto User que será passado para o save
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            // Obtém o objeto User que foi capturado
            User capturedUser = userCaptor.getValue();

            // Simula o comportamento do repositório atribuindo um ID
            capturedUser.setId(1L);

            return capturedUser;
        });

        when(userMapper.entityToResponse(any(User.class))).thenReturn(expectedResponse);

        // Act - execução do método que está sendo testado
        UserResponseDTO result = userService.registerUser(userRequestDTO);

        // Assert - verificações

        // Verifica se o resultado é o esperado
        assertEquals(expectedResponse.id(), result.id());
        assertEquals(expectedResponse.email(), result.email());

        // Verifica se os métodos foram chamados corretamente
        verify(userRepository).existsByEmail(userRequestDTO.email());
        verify(passwordEncoder).encode(userRequestDTO.password());
        verify(userRepository).save(any(User.class));
        verify(userMapper).entityToResponse(any(User.class));

        // Verifica se o usuário capturado tem os valores corretos
        User savedUser = userCaptor.getValue();
        assertEquals(userRequestDTO.email(), savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void getUserByEmail() {
    }
}