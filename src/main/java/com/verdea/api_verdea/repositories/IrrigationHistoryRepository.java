package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.dtos.dashboardDto.SoilMoistureChartDTO;
import com.verdea.api_verdea.entities.IrrigationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IrrigationHistoryRepository extends JpaRepository<IrrigationHistory, Long> {
    @Query("""
        SELECT h FROM IrrigationHistory h
        JOIN h.plant p
        WHERE p.user.id = :userId
    """)
    Page<IrrigationHistory> findAllByUserId(Long userId, Pageable pageable);
    Optional<IrrigationHistory> findTopByPlant_UserIdOrderByCreatedAtDesc(Long userId);
    @Query("""
        SELECT AVG(h.soilMoisture)
        FROM IrrigationHistory h
        JOIN h.plant p
        WHERE p.user.id = :userId
    """)
    Optional<Double> findAverageSoilMoistureByUserId(Long userId);

    @Query("""
        SELECT h.createdAt, h.soilMoisture
        FROM IrrigationHistory h
        JOIN h.plant p
        WHERE p.user.id = :userId
        ORDER BY h.createdAt ASC
    """)
    List<Object[]> findSoilMoistureDataByUserId(Long userId);

}
