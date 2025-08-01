package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.entities.Plant;
import com.verdea.api_verdea.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findAllByUser(User user);
}
