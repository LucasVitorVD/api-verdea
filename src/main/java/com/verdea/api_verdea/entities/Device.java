package com.verdea.api_verdea.entities;

import com.verdea.api_verdea.enums.DeviceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do dispositivo obrigatório")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Endereço MAC obrigatório")
    @Column(name = "mac_address", nullable = false, unique = true)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status é obrigatório")
    @Column(nullable = false)
    private DeviceStatus status;

    @Min(0)
    @Max(100)
    private Integer batteryLevel;

    private LocalDateTime lastConnection;

    @DecimalMin("0.0")
    @Column(name = "tank_level")
    private Double tankLevel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    private User user;
}
