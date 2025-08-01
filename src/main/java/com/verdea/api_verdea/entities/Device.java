package com.verdea.api_verdea.entities;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    private User user;

    @OneToOne(mappedBy = "device")
    private Plant plant;
}
