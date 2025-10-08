package com.verdea.api_verdea.services.plant;

import com.verdea.api_verdea.dtos.deviceDto.DeviceSummary;
import com.verdea.api_verdea.dtos.plantDto.PlantRequestDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.PlantWateringTime;
import com.verdea.api_verdea.entities.User;
import com.verdea.api_verdea.exceptions.DeviceAlreadyAssignedException;
import com.verdea.api_verdea.exceptions.DeviceNotFoundException;
import com.verdea.api_verdea.exceptions.PlantNotFoundException;
import com.verdea.api_verdea.exceptions.UserNotFoundException;
import com.verdea.api_verdea.repositories.DeviceRepository;
import com.verdea.api_verdea.repositories.PlantRepository;
import com.verdea.api_verdea.repositories.UserRepository;
import com.verdea.api_verdea.services.mqtt.MqttService;
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
    private final MqttService mqttService;

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
        plant.setWateringFrequency(plantRequestDTO.wateringFrequency());
        plant.setIdealSoilMoisture(plantRequestDTO.idealSoilMoisture());
        plant.setMode(plantRequestDTO.mode());
        plant.setImageUrl(plantRequestDTO.imageUrl());
        plant.setDevice(device);
        plant.setUser(user);

        if (plantRequestDTO.wateringTimes() != null) {
            plantRequestDTO.wateringTimes().forEach(time -> {
                PlantWateringTime plantWateringTime = new PlantWateringTime();
                plantWateringTime.setWateringTime(time);
                plant.getWateringTimes().add(plantWateringTime);
                plantWateringTime.setPlant(plant);
            });
        }

        device.setPlant(plant);

        Plant savedPlant = plantRepository.save(plant);

        mqttService.sendPlantConfigToDevice(device.getMacAddress(), mapToPlantResponseDTO(savedPlant));
    }

    public List<PlantResponseDTO> getPlantsByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        List<Plant> plants = plantRepository.findAllByUser(user);

        return plants.stream()
                .map(this::mapToPlantResponseDTO)
                .toList();
    }

    @Transactional
    public PlantResponseDTO getPlantById(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new PlantNotFoundException("Planta não encontrada."));

        return mapToPlantResponseDTO(plant);
    }

    @Transactional
    public PlantResponseDTO updatePlantData(Long plantId, PlantRequestDTO plantRequestDTO) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new PlantNotFoundException("Planta não encontrada."));

        if (plantRequestDTO.deviceMacAddress() != null &&
                !plantRequestDTO.deviceMacAddress().equals(plant.getDevice().getMacAddress())) {

            Device newDevice = deviceRepository.findByMacAddress(plantRequestDTO.deviceMacAddress())
                    .orElseThrow(() -> new DeviceNotFoundException("Dispositivo não encontrado."));

            if (newDevice.getPlant() != null) {
                throw new DeviceAlreadyAssignedException("Este dispositivo já está vinculado a uma planta.");
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

        plant.setName(plantRequestDTO.name());
        plant.setSpecies(plantRequestDTO.species());
        plant.setLocation(plantRequestDTO.location());
        plant.setNotes(plantRequestDTO.notes());
        plant.setWateringFrequency(plantRequestDTO.wateringFrequency());
        plant.setIdealSoilMoisture(plantRequestDTO.idealSoilMoisture());
        plant.setMode(plantRequestDTO.mode());
        plant.setImageUrl(plantRequestDTO.imageUrl());

        plant.getWateringTimes().clear();

        if (plantRequestDTO.wateringTimes() != null) {
            plantRequestDTO.wateringTimes().forEach(time -> {
                PlantWateringTime wateringTime = new PlantWateringTime();
                wateringTime.setWateringTime(time);
                wateringTime.setPlant(plant);
                plant.getWateringTimes().add(wateringTime);
            });
        }

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
    public void deletePlant(Long plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new PlantNotFoundException("Planta não encontrada."));

        Device device = plant.getDevice();

        if (device != null) {
            String cleanMac = device.getMacAddress().replace(":", "");
            mqttService.publish(
                    "verdea/commands/" + cleanMac,
                    "{ \"command\": \"DELETE_PLANT\" }"
            );

            plant.setDevice(null);
            device.setPlant(null);
            deviceRepository.save(device);
        }

        plantRepository.delete(plant);
    }

    private PlantResponseDTO mapToPlantResponseDTO(Plant plant) {
        DeviceSummary deviceSummary = null;

        if (plant.getDevice() != null) {
            deviceSummary = new DeviceSummary(
                    plant.getDevice().getId(),
                    plant.getDevice().getName(),
                    plant.getDevice().getMacAddress(),
                    plant.getDevice().getCreatedAt()
            );
        }

        List<String> wateringTimes = plant.getWateringTimes()
                .stream()
                .map(PlantWateringTime::getWateringTime)
                .toList();

        return new PlantResponseDTO(
                plant.getId(),
                plant.getName(),
                plant.getSpecies(),
                plant.getLocation(),
                plant.getNotes(),
                wateringTimes,
                plant.getWateringFrequency(),
                plant.getIdealSoilMoisture(),
                plant.getMode(),
                plant.getImageUrl(),
                deviceSummary
        );
    }
}
