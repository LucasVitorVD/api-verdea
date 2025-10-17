package com.verdea.api_verdea.repositories;

import com.verdea.api_verdea.entities.Device;
import com.verdea.api_verdea.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    boolean existsByMacAddress(String macAddress);
    Optional<Device> findByMacAddress(String macAddress);
    List<Device> findAllByUser(User user);
    List<Device> findAllByUserIsNotNull();
}
