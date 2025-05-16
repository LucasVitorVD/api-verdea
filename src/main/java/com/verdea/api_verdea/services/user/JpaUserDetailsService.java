package com.verdea.api_verdea.services.user;

import com.verdea.api_verdea.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username).map(user -> User.builder()
                .username(username)
                .password(user.getPassword())
                .build()
        ).orElseThrow(() -> new UsernameNotFoundException("Usuário com o email " + username + " não encontrado."));
    }
}