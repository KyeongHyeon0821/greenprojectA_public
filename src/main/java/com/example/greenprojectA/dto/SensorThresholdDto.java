package com.example.greenprojectA.dto;

import com.example.greenprojectA.entity.SensorThreshold;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorThresholdDto {

    private Integer sensorThresholdId;  // 임계값 ID

    private Integer companyId;    // 회사 ID

    private String deviceCode;    // 디바이스 코드

    private Integer valueNo;   // value1~13을 1~13으로

    private Double minValue;      // 최소 임계값

    private Double maxValue;      // 최대 임계값

    private LocalDateTime createdAt;  // 생성 일시

    private String createdBy;     // 생성자

    // Entity to Dto
    public static SensorThresholdDto createSensorThresholdDto(Optional<SensorThreshold> optionalSensorThreshold) {
        return SensorThresholdDto.builder()
                .sensorThresholdId(optionalSensorThreshold.get().getSensorThresholdId())
                .companyId(optionalSensorThreshold.get().getCompanyId())
                .deviceCode(optionalSensorThreshold.get().getDeviceCode())
                .valueNo(optionalSensorThreshold.get().getValueNo())
                .minValue(optionalSensorThreshold.get().getMinValue())
                .maxValue(optionalSensorThreshold.get().getMaxValue())
                .createdAt(optionalSensorThreshold.get().getCreatedAt())
                .createdBy(optionalSensorThreshold.get().getCreatedBy())
                .build();
    }
}
