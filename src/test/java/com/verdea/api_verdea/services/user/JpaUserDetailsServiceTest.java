package com.verdea.api_verdea.services.user;

import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
class JpaUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService jpaUserDetailsService;

    @Test
    @DisplayName("Should load user by email successfully")
    void loadUserByUsernameCase1() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setPassword("encodedPassword");

        String email = user.getEmail();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        assertEquals(userDetails.getUsername(), email);
        assertEquals(userDetails.getPassword(), "encodedPassword");
    }

    @Test
    @DisplayName("Should throw a UsernameNotFoundException if user does not exists")
    void loadUserByUsernameCase2() {
        String email = "myemail@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> jpaUserDetailsService.loadUserByUsername(email));
    }
}