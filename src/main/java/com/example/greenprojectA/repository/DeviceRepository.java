package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Integer> {

  List<Device> findAllByOrderByIdxDesc();

  Device findByDeviceCode(String deviceCode);

  List<Device> findByCompanyIdOrderByIdxDesc(Long companyId);
}
