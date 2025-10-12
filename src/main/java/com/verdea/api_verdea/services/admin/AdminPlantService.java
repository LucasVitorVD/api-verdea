package com.verdea.api_verdea.services.admin;

import com.verdea.api_verdea.dtos.plantDto.PlantRequestDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.PlantWateringTime;
import com.verdea.api_verdea.exceptions.DeviceAlreadyAssignedException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.exceptions.PlantNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.PlantRepository;
import com.verdea.api_verdea.services.mqtt.MqttService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPlantService {
    private final PlantRepository plantRepository;
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;

    public List<PlantResponseDTO> getAllPlants() {
        return plantRepository.findAll().stream()
                .map(this::mapToPlantResponseDTO)
                .toList();
    }

    public PlantResponseDTO getPlantById(Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException("Planta não encontrada."));
        return mapToPlantResponseDTO(plant);
    }

    @Transactional
    public PlantResponseDTO createPlant(PlantRequestDTO dto) {
        Device device = null;

        if (dto.deviceMacAddress() != null) {
            device = deviceRepository.findByMacAddress(dto.deviceMacAddress())
                    .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

            if (device.getPlant() != null) {
                throw new DeviceAlreadyAssignedException("Dispositivo já vinculado a outra planta.");
            }
        }

        Plant plant = new Plant();
        plant.setName(dto.name());
        plant.setSpecies(dto.species());
        plant.setLocation(dto.location());
        plant.setNotes(dto.notes());
        plant.setWateringFrequency(dto.wateringFrequency());
        plant.setIdealSoilMoisture(dto.idealSoilMoisture());
        plant.setMode(dto.mode());
        plant.setImageUrl(dto.imageUrl());
        plant.setDevice(device);

        if (device != null) {
            device.setPlant(plant);
        }

        Plant savedPlant = plantRepository.save(plant);

        if (device != null) {
            mqttService.sendPlantConfigToDevice(device.getMacAddress(), mapToPlantResponseDTO(savedPlant));
        }

        return mapToPlantResponseDTO(savedPlant);
    }

    @Transactional
    public PlantResponseDTO updatePlant(Long id, PlantRequestDTO dto) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException("Planta não encontrada."));

        if (dto.deviceMacAddress() != null &&
                (plant.getDevice() == null || !dto.deviceMacAddress().equals(plant.getDevice().getMacAddress()))) {

            Device newDevice = deviceRepository.findByMacAddress(dto.deviceMacAddress())
                    .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

            if (newDevice.getPlant() != null) {
                throw new DeviceAlreadyAssignedException("Dispositivo já vinculado a outra planta.");
            }

            Device oldDevice = plant.getDevice();
            if (oldDevice != null) {
                oldDevice.setPlant(null);
                deviceRepository.save(oldDevice);
            }

            newDevice.setPlant(plant);
            plant.setDevice(newDevice);
            deviceRepository.save(newDevice);
        }

        plant.setName(dto.name());
        plant.setSpecies(dto.species());
        plant.setLocation(dto.location());
        plant.setNotes(dto.notes());
        plant.setWateringFrequency(dto.wateringFrequency());
        plant.setIdealSoilMoisture(dto.idealSoilMoisture());
        plant.setMode(dto.mode());
        plant.setImageUrl(dto.imageUrl());

        Plant updatedPlant = plantRepository.save(plant);

        if (updatedPlant.getDevice() != null) {
            mqttService.sendPlantConfigToDevice(
                    updatedPlant.getDevice().getMacAddress(),
                    mapToPlantResponseDTO(updatedPlant)
            );
        }

        return mapToPlantResponseDTO(updatedPlant);
    }

    @Transactional
    public void deletePlant(Long id) {
        Plant plant = plantRepository.findById(id)
                .orElseThrow(() -> new PlantNotFoundException("Planta não encontrada."));

        Device device = plant.getDevice();
        if (device != null) {
            device.setPlant(null);
            deviceRepository.save(device);
        }

        plantRepository.delete(plant);
    }

    // Mapeamento privado
    private PlantResponseDTO mapToPlantResponseDTO(Plant plant) {
        return new PlantResponseDTO(
                plant.getId(),
                plant.getName(),
                plant.getSpecies(),
                plant.getLocation(),
                plant.getNotes(),
                plant.getWateringTimes().stream().map(PlantWateringTime::getWateringTime).toList(),
                plant.getWateringFrequency(),
                plant.getIdealSoilMoisture(),
                plant.getMode(),
                plant.getImageUrl(),
                null // device summary opcional
        );
    }
}