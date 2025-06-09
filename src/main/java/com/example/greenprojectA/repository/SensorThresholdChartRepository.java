package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorThresholdChartRepository extends JpaRepository<SensorThreshold, Integer> {
    SensorThreshold findTopByCompanyIdAndDeviceCodeAndValueNoOrderByCreatedAtDesc(
            Integer companyId, String deviceCode, Integer valueNo
    );
}