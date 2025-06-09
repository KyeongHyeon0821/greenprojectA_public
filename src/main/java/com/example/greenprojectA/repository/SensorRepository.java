package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.Company;
import com.example.greenprojectA.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {

    @Query(value = """
    SELECT s.*
    FROM sensor s
    INNER JOIN (
        SELECT device_code, MAX(measure_datetime) AS latest_time
        FROM sensor
        GROUP BY device_code
        ORDER BY device_code
    ) latest ON s.device_code = latest.device_code AND s.measure_datetime = latest.latest_time
    """, nativeQuery = true)
    List<Sensor> getLatestSensorData();

    @Query(value = """
    SELECT s.*
    FROM sensor s
    INNER JOIN (
        SELECT device_code, MAX(measure_datetime) AS latest_time
        FROM sensor
        WHERE company_id = :companyId
        GROUP BY device_code
        ORDER BY device_code
    ) latest ON s.device_code = latest.device_code AND s.measure_datetime = latest.latest_time
    """, nativeQuery = true)
    List<Sensor> getLatestSensorDataDtoByCompany(Long companyId);

    @Query("SELECT s FROM Sensor s " +
            "WHERE s.deviceCode = :deviceCode " +
            "AND s.measureDatetime BETWEEN :startDate AND :endDate")
    List<Sensor> findByDeviceCodeAndDateRange(@Param("deviceCode") String deviceCode,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}
