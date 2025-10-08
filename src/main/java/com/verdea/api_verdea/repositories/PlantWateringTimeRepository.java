package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.PlantWateringTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantWateringTimeRepository extends JpaRepository<PlantWateringTime, Long> {
    List<PlantWateringTime> findByPlant(Plant plant);
}