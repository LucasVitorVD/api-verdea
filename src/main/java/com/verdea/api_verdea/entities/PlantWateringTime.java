package com.verdea.api_verdea.entities;

import com.verdea.api_verdea.enums.WateringFrequency;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plant_watering_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantWateringTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String wateringTime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;
}
