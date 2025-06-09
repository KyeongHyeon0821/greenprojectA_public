package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorChartRepository extends JpaRepository<Sensor, Long> {
    List<Sensor> findByDeviceCodeAndMeasureDatetimeBetween(String deviceCode, LocalDateTime start, LocalDateTime end);


    List<Sensor> findByMeasureDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}