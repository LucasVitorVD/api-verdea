package com.verdea.api_verdea.services.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.dtos.plantDto.PlantResponseDTO;
import com.verdea.api_verdea.enums.DeviceStatus;
import com.verdea.api_verdea.exceptions.MqttCommunicationException;
import com.verdea.api_verdea.services.device.DeviceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {
    private final DeviceService deviceService;

    private final MqttClient mqttClient;
    private final MqttConnectOptions mqttConnectOptions;
    private static final String TOPIC_REGISTRATION = "verdea/device/register";
    private static final String TOPIC_STATUS_WILDCARD = "verdea/status/#";

    @PostConstruct
    public void init() {
        try {
            // Define o callback para receber mensagens
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("Conexão MQTT perdida.", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    handleIncomingMessage(topic, message.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Não é usado para QoS 0
                }
            });

            if (!mqttClient.isConnected()) {
                log.info("Iniciando conexão com o broker MQTT...");
                mqttClient.connect(mqttConnectOptions);
                mqttClient.subscribe(TOPIC_REGISTRATION, 1);
                mqttClient.subscribe(TOPIC_STATUS_WILDCARD, 1);
                log.info("✅ Conectado e inscrito nos tópicos: {}, {}", TOPIC_REGISTRATION, TOPIC_STATUS_WILDCARD);
            }
        } catch (MqttException e) {
            log.error("❌ Falha crítica na conexão MQTT: {}", e.getMessage());
            throw new MqttCommunicationException("Falha na inicialização do serviço MQTT", e);
        }
    }

    private void handleIncomingMessage(String topic, byte[] payload) {
        String payloadString = new String(payload);
        log.info("📩 Mensagem recebida no tópico '{}': {}", topic, payloadString);

        if (topic.equals(TOPIC_REGISTRATION)) {
            handleDeviceRegistration(payloadString);
        }

        if (topic.startsWith("verdea/status/")) {
            handleStatusUpdate(payloadString);
        }
    }

    private void handleDeviceRegistration(String payloadString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DeviceRequestDTO deviceDto = objectMapper.readValue(payloadString, DeviceRequestDTO.class);
            deviceService.registerDevice(deviceDto);
            log.info("✅ Dispositivo com MAC '{}' registrado com sucesso.", deviceDto.macAddress());
        } catch (Exception e) {
            log.error("❌ Erro ao processar mensagem de registro: {}", e.getMessage());
        }
    }

    private void handleStatusUpdate(String payloadString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map statusData = objectMapper.readValue(payloadString, Map.class);
            String macAddress = statusData.get("macAddress").toString();
            String status = statusData.get("status").toString();

            if (macAddress != null && status != null) {
                deviceService.updateDeviceStatus(macAddress, DeviceStatus.valueOf(status.toUpperCase()));
                log.info("✅ Status do dispositivo '{}' atualizado para '{}' no banco de dados.", macAddress, status);
            }
        } catch (Exception e) {
            log.error("❌ Erro ao processar mensagem de status: {}", e.getMessage());
        }
    }

    public void sendPlantConfigToDevice(String deviceMacAddress, PlantResponseDTO plant) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String payload = objectMapper.writeValueAsString(Map.of(
                    "mode", plant.mode(),
                    "wateringTime", plant.wateringTime(),
                    "wateringFrequency", plant.wateringFrequency(),
                    "idealSoilMoisture", plant.idealSoilMoisture()
            ));

            String cleanMac = deviceMacAddress.replace(":", "");
            String topic = "verdea/commands/" + cleanMac;

            publish(topic, payload);
            log.info("✅ Configuração de irrigação enviada para dispositivo '{}'", deviceMacAddress);
        } catch (Exception e) {
            log.error("❌ Erro ao enviar configuração de irrigação para '{}': {}", deviceMacAddress, e.getMessage());
            throw new MqttCommunicationException("Erro ao enviar configuração de irrigação", e);
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("🚫 Conexão MQTT encerrada.");
            }
        } catch (MqttException e) {
            log.error("Erro ao desconectar do broker MQTT: {}", e.getMessage());
        }
    }

    public void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(0);

            mqttClient.publish(topic, message);
            log.info("📤 Mensagem publicada: Tópico='{}', Payload='{}'", topic, payload);
        } catch (MqttException e) {
            log.error("❌ Erro ao publicar mensagem no tópico '{}': {}", topic, e.getMessage());
            throw new MqttCommunicationException("Erro ao publicar comando MQTT", e);
        }
    }
}