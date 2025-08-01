package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.entities.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
}
