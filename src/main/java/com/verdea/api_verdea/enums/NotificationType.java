package com.verdea.api_verdea.enums;

public enum NotificationType {
    LOW_WATER("Nível de água baixo"),
    IRRIGATION_COMPLETE("Irrigação concluída"),
    DEVICE_OFFLINE("Dispositivo desconectado"),
    SYSTEM_ERROR("Erro no sistema");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
