package com.example.greenprojectA.entity;

import com.example.greenprojectA.dto.SensorDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sensor")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sensorId; // 센서 ID

    private Integer companyId; // 회사 ID

    private String deviceCode;  // 디바이스 코드

    private LocalDateTime measureDatetime;  // 측정 일시

    private Double value1;  // 실내온도
    private Double value2;  // 상대습도
    private Double value3;  // 이산화탄소
    private Double value4;  // 유기화합물VOC
    private Double value5;  // 초미세먼지 PM1.0
    private Double value6;  // 초미세먼지 PM2.5
    private Double value7;  // 초미세먼지 PM10
    private Double value8;  // 온도_1
    private Double value9;  // 온도_2
    private Double value10; // 온도_3
    private Double value11; // 소음
    private Double value12; // 온도(비접촉)
    private Double value13; // 조도


    // Dto to Entity
    public static Sensor createSensor(SensorDto sensorDto) {
        return Sensor.builder()
                .companyId(sensorDto.getCompanyId())
                .deviceCode(sensorDto.getDeviceCode())
                .measureDatetime(sensorDto.getMeasureDatetime())
                .value1(sensorDto.getValue1())
                .value2(sensorDto.getValue2())
                .value3(sensorDto.getValue3())
                .value4(sensorDto.getValue4())
                .value5(sensorDto.getValue5())
                .value6(sensorDto.getValue6())
                .value7(sensorDto.getValue7())
                .value8(sensorDto.getValue8())
                .value9(sensorDto.getValue9())
                .value10(sensorDto.getValue10())
                .value11(sensorDto.getValue11())
                .value12(sensorDto.getValue12())
                .value13(sensorDto.getValue13())
                .build();
    }

}
