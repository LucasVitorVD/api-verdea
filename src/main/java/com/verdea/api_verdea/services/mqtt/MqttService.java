package com.verdea.api_verdea.services.mqtt;

import com.verdea.api_verdea.exceptions.MqttCommunicationException;
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

    private final MqttClient mqttClient;
    private final MqttConnectOptions mqttConnectOptions;
    private final String clientIdentifier = "verdea-backend";

    @PostConstruct
    public void init() {
        try {
            if (!mqttClient.isConnected()) {
                log.info("Iniciando conexão com o broker MQTT...");
                mqttClient.connect(mqttConnectOptions);
                log.info("✅ Conexão MQTT estabelecida com sucesso!");
            }
        } catch (MqttException e) {
            log.error("❌ Falha crítica na conexão MQTT: {}", e.getMessage());
            throw new MqttCommunicationException("Falha na inicialização do serviço MQTT", e);
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