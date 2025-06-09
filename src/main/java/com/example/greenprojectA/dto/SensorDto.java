package com.example.greenprojectA.dto;

import com.example.greenprojectA.entity.Sensor;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorDto {

    private Integer sensorId; // 센서 ID

    private Integer companyId;  // 회사 ID

    private String deviceCode;  // 디바이스 코드

    private LocalDateTime measureDatetime;  // 측정 일시

    private Double value1;  // 실내온도
    private Boolean value1Event; // 실내온도 이벤트 발생 여부

    private Double value2;  // 상대습도
    private Boolean value2Event; // 상대습도 이벤트 발생 여부

    private Double value3;  // 이산화탄소
    private Boolean value3Event; // 이산화탄소 이벤트 발생 여부

    private Double value4;  // 유기화합물VOC
    private Boolean value4Event; // 유기화합물VOC 이벤트 발생 여부

    private Double value5;  // 초미세먼지 PM1.0
    private Boolean value5Event; // 초미세먼지 PM1.0 이벤트 발생 여부

    private Double value6;  // 초미세먼지 PM2.5
    private Boolean value6Event; // 초미세먼지 PM2.5 이벤트 발생 여부

    private Double value7;  // 초미세먼지 PM10
    private Boolean value7Event; // 초미세먼지 PM10 이벤트 발생 여부

    private Double value8;  // 온도_1
    private Boolean value8Event; // 온도_1 이벤트 발생 여부

    private Double value9;  // 온도_2
    private Boolean value9Event; // 온도_2 이벤트 발생 여부

    private Double value10; // 온도_3
    private Boolean value10Event; // 온도_3 이벤트 발생 여부

    private Double value11; // 소음
    private Double value12; // 온도(비접촉)
    private Double value13; // 조도

    // Entity to Dto
    public static SensorDto createSensorDto(Optional<Sensor> optionalSensor) {
        return SensorDto.builder()
                .sensorId(optionalSensor.get().getSensorId())
                .companyId(optionalSensor.get().getCompanyId())
                .deviceCode(optionalSensor.get().getDeviceCode())
                .measureDatetime(optionalSensor.get().getMeasureDatetime())
                .value1(optionalSensor.get().getValue1())
                .value2(optionalSensor.get().getValue2())
                .value3(optionalSensor.get().getValue3())
                .value4(optionalSensor.get().getValue4())
                .value5(optionalSensor.get().getValue5())
                .value6(optionalSensor.get().getValue6())
                .value7(optionalSensor.get().getValue7())
                .value8(optionalSensor.get().getValue8())
                .value9(optionalSensor.get().getValue9())
                .value10(optionalSensor.get().getValue10())
                .value11(optionalSensor.get().getValue11())
                .value12(optionalSensor.get().getValue12())
                .value13(optionalSensor.get().getValue13())
                .build();
    }

}
