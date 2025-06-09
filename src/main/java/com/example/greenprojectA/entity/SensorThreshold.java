package com.example.greenprojectA.entity;

import com.example.greenprojectA.dto.SensorThresholdDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_threshold")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(value = {AuditingEntityListener.class})
public class SensorThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_threshold_id")
    private Integer sensorThresholdId;     // 임계값 ID

    private Integer companyId;             // 회사 ID

    private String deviceCode;             // 디바이스 코드

    private Integer valueNo;               // value1~13을 1~13으로

    private Double minValue;               // 최소 임계값

    private Double maxValue;               // 최대 임계값

    @CreatedDate
    private LocalDateTime createdAt;       // 생성 일시

    private String createdBy;              // 생성자

    // Dto to Entity
    public static SensorThreshold createSensorThreshold(SensorThresholdDto sensorThresholdDto) {
        return SensorThreshold.builder()
                .deviceCode(sensorThresholdDto.getDeviceCode())
                .companyId(sensorThresholdDto.getCompanyId())
                .valueNo(sensorThresholdDto.getValueNo())
                .minValue(sensorThresholdDto.getMinValue())
                .maxValue(sensorThresholdDto.getMaxValue())
                .createdAt(sensorThresholdDto.getCreatedAt())
                .createdBy(sensorThresholdDto.getCreatedBy())
                .build();
    }

}
