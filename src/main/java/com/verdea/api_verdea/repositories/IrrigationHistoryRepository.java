package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.entities.IrrigationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IrrigationHistoryRepository extends JpaRepository<IrrigationHistory, Long> {
    @Query("""
        SELECT h FROM IrrigationHistory h
        JOIN h.plant p
        WHERE p.user.id = :userId
    """)
    Page<IrrigationHistory> findAllByUserId(Long userId, Pageable pageable);
}
