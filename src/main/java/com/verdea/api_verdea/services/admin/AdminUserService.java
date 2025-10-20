package com.verdea.api_verdea.services.admin;

import com.verdea.api_verdea.dtos.userDto.UpdateUserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserRequestDTO;
import com.verdea.api_verdea.dtos.userDto.UserResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.enums.Role;
import com.verdea.api_verdea.exceptions.EmailAlreadyInUseException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.mappers.UserMapper;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::entityToResponse)
                .toList();
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        return userMapper.entityToResponse(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyInUseException("Este email já está em uso.");
        }

        User newUser = new User();
        newUser.setEmail(dto.email());
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setRole(Role.USER);

        User savedUser = userRepository.save(newUser);
        return userMapper.entityToResponse(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        if (dto.email() != null && !dto.email().equals(user.getEmail()) &&
                userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyInUseException("Este email já está em uso.");
        }

        if (dto.email() != null) user.setEmail(dto.email());
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.entityToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        List<Device> devices = deviceRepository.findAllByUser(user);
        devices.forEach(device -> device.setUser(null));
        deviceRepository.saveAll(devices);

        if (user.getPlants() != null && !user.getPlants().isEmpty()) {
            user.getPlants().forEach(plant -> {
                if (plant.getDevice() != null) {
                    plant.getDevice().setPlant(null);
                    deviceRepository.save(plant.getDevice());
                    plant.setDevice(null);
                }
            });
        }

        userRepository.delete(user);
    }
}