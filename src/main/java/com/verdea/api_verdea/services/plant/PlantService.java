package com.verdea.api_verdea.services.plant;

import com.verdea.api_verdea.dtos.plantDto.PlantRequestDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.DeviceAlreadyAssignedException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.PlantRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {
    private final PlantRepository plantRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addPlant(PlantRequestDTO plantRequestDTO, String userEmail) {
        Device device = deviceRepository.findByMacAddress(plantRequestDTO.deviceMacAddress())
                .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

        if (device.getPlant() != null) {
            throw new DeviceAlreadyAssignedException("Este dispositivo já está vinculado a uma planta.");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        Plant plant = new Plant();
        plant.setName(plantRequestDTO.name());
        plant.setSpecies(plantRequestDTO.species());
        plant.setLocation(plantRequestDTO.location());
        plant.setNotes(plantRequestDTO.notes());
        plant.setWateringTime(plantRequestDTO.wateringTime());
        plant.setWateringFrequency(plantRequestDTO.wateringFrequency());
        plant.setIdealSoilMoisture(plantRequestDTO.idealSoilMoisture());
        plant.setImage_url(plantRequestDTO.imageUrl());
        plant.setDevice(device);
        plant.setUser(user);

        device.setPlant(plant);

        plantRepository.save(plant);
    }

    public List<PlantResponseDTO> getPlantsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        List<Plant> plants = plantRepository.findAllByUser(user);

        return plants.stream()
                .map(plant -> new PlantResponseDTO(
                        plant.getId(),
                        plant.getName(),
                        plant.getSpecies(),
                        plant.getLocation(),
                        plant.getNotes(),
                        plant.getWateringTime(),
                        plant.getWateringFrequency(),
                        plant.getIdealSoilMoisture(),
                        plant.getImage_url(),
                        plant.getDevice()
                ))
                .toList();
    }
}
