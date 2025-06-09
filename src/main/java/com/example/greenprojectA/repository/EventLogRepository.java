package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface EventLogRepository extends JpaRepository<EventLog, Integer> {
    @Query("SELECT COUNT(e) FROM EventLog e " +
            "WHERE e.deviceCode = :deviceCode " +
            "AND e.valueNo = :valueNo " +
            "AND e.measureDatetime BETWEEN :startDate AND :endDate")
    int countByDeviceAndValueNoAndRange(@Param("deviceCode") String deviceCode,
                                        @Param("valueNo") int valueNo,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
