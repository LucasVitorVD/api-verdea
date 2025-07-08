package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.entities.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    boolean existsByMacAddress(String macAddress);
}
