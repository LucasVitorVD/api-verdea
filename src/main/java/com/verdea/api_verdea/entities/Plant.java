package com.verdea.api_verdea.entities;


import com.verdea.api_verdea.enums.WateringFrequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "plants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String wateringTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WateringFrequency wateringFrequency;

    @Column(nullable = false)
    private Double idealSoilMoisture;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    private LocalDateTime lastWatered;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device;
}
