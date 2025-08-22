package com.verdea.api_verdea.services.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.verdea.api_verdea.dtos.deviceDto.DeviceRequestDTO;
import com.verdea.api_verdea.exceptions.MqttCommunicationException;
import com.verdea.api_verdea.services.device.DeviceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {
    private final DeviceService deviceService;

    private final MqttClient mqttClient;
    private final MqttConnectOptions mqttConnectOptions;
    private final String clientIdentifier = "verdea-backend";
    private static final String TOPIC_REGISTRATION = "verdea/device/register";

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
                log.info("✅ Conectado e inscrito no tópico de registro: {}", TOPIC_REGISTRATION);            }
        } catch (MqttException e) {
            log.error("❌ Falha crítica na conexão MQTT: {}", e.getMessage());
            throw new MqttCommunicationException("Falha na inicialização do serviço MQTT", e);
        }
    }

    private void handleIncomingMessage(String topic, byte[] payload) {
        String payloadString = new String(payload);
        log.info("📩 Mensagem recebida no tópico '{}': {}", topic, payloadString);

        if (topic.equals(TOPIC_REGISTRATION)) {
            try {
                // Converte a mensagem JSON para um DTO de registro
                ObjectMapper objectMapper = new ObjectMapper();
                DeviceRequestDTO deviceDto = objectMapper.readValue(payloadString, DeviceRequestDTO.class);

                // Usa o serviço de dispositivo para registrar
                deviceService.registerDevice(deviceDto);
                log.info("✅ Dispositivo com MAC '{}' registrado com sucesso.", deviceDto.macAddress());
            } catch (Exception e) {
                log.error("❌ Erro ao processar mensagem de registro: {}", e.getMessage());
            }
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