package com.verdea.api_verdea.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.verdea.api_verdea.enums.DeviceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
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

    @Column(name = "current_ip")
    private String currentIp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToOne(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Plant plant;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IrrigationHistory> irrigationHistory = new ArrayList<>();
}
