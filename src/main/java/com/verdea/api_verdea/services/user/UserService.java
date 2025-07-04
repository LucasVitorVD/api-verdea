package com.verdea.api_verdea.services.user;

import com.verdea.api_verdea.dtos.userDto.UpdateUserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.EmailAlreadyInUseException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.mappers.UserMapper;
import com.verdea.api_verdea.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyInUseException("Este email já está em uso.");
        }

        User newUser = new User();
        newUser.setEmail(dto.email());
        newUser.setPassword(passwordEncoder.encode(dto.password()));

        User savedUser = userRepository.save(newUser);

        return userMapper.entityToResponse(savedUser);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("A conta do usuário foi apagada ou inativada"));

        return userMapper.entityToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        userRepository.delete(user);
    }

    @Transactional
    public UserResponseDTO updateUserInfo(String currentEmail, UpdateUserRequestDTO dto) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        if (user.getEmail().equals(dto.email())) {
            throw new EmailAlreadyInUseException("Este e-mail já está em uso.");
        }

        if (dto.email() != null) {
            user.setEmail(dto.email());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.entityToResponse(updatedUser);
    }
}
