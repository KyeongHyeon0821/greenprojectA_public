package com.example.greenprojectA.service;

import com.example.greenprojectA.dto.SensorChartResponseDto;
import com.example.greenprojectA.dto.SensorDto;
import com.example.greenprojectA.entity.Sensor;
import com.example.greenprojectA.entity.SensorThreshold;
import com.example.greenprojectA.repository.SensorChartRepository;
import com.example.greenprojectA.repository.SensorRepository;
import com.example.greenprojectA.repository.SensorThresholdChartRepository;
import com.example.greenprojectA.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorChartRepository sensorChartRepository;
    private final SensorThresholdChartRepository thresholdChartRepository;
    private final SensorRepository sensorRepository;
    private final ThresholdRepository thresholdRepository;

    public List<SensorChartResponseDto> getChartData(String deviceCode, String valueKey, LocalDateTime start, LocalDateTime end) {
        List<Sensor> sensors = sensorChartRepository.findByDeviceCodeAndMeasureDatetimeBetween(deviceCode, start, end);
        // (1) valueKey → valueNo 변환
        int valueNo = Integer.parseInt(valueKey.replace("value", ""));

        // (2) 임계값 조회 (companyId는 예시로 1 고정)
        SensorThreshold threshold = thresholdChartRepository.findTopByCompanyIdAndDeviceCodeAndValueNoOrderByCreatedAtDesc(
                1000003, deviceCode, valueNo
        );


        // 차트 출력
        List<SensorChartResponseDto> result = new ArrayList<>();
        for (Sensor sensor : sensors) {
            LocalDateTime time = sensor.getMeasureDatetime();
            Double value = extractValue(sensor, valueKey);
            if (value != null) {
                String label = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                result.add(new SensorChartResponseDto(label, value));
            }
        }

        // (3) 임계값이 존재하면 리스트에 추가
        if (threshold != null) {
            String minLabel = "minThreshold";
            String maxLabel = "maxThreshold";
            result.add(new SensorChartResponseDto(minLabel, threshold.getMinValue()));
            result.add(new SensorChartResponseDto(maxLabel, threshold.getMaxValue()));
        }

        return result;
    }

    private Double extractValue(Sensor sensor, String key) {
        return switch (key) {
            case "value1" -> sensor.getValue1();
            case "value2" -> sensor.getValue2();
            case "value3" -> sensor.getValue3();
            case "value4" -> sensor.getValue4();
            case "value5" -> sensor.getValue5();
            case "value6" -> sensor.getValue6();
            case "value7" -> sensor.getValue7();
            case "value8" -> sensor.getValue8();
            case "value9" -> sensor.getValue9();
            case "value10" -> sensor.getValue10();
            case "value11" -> sensor.getValue11();
            case "value12" -> sensor.getValue12();
            case "value13" -> sensor.getValue13();
            default -> null;
        };
    }


    // 전체 센서 데이터 조회
    public List<SensorDto> getLatestSensorDataDto() {
        List<Sensor> sensorList = sensorRepository.getLatestSensorData();
        List<SensorDto> sensorDtoList = new ArrayList<>();

        for(Sensor sensor: sensorList) {
            SensorDto dto = SensorDto.createSensorDto(Optional.of(sensor));


            // 반복문으로 value1 ~ value10에 대해 이벤트 발생 여부 체크
            for (int i = 1; i <= 10; i++) {
                Double value = getValueByNo(sensor, i);

                // 최근 등록 임계값 조회
                SensorThreshold threshold = thresholdRepository
                        .findTopByCompanyIdAndDeviceCodeAndValueNoOrderByCreatedAtDesc(
                                sensor.getCompanyId(), sensor.getDeviceCode(), i);

                // 냉장고 온도(value 8~10)는 특수 예외 처리
                if ((i >= 8 && i <= 10) && (value == -242 || value == 988.7)) {
                    setValueEventByNo(dto, i, false);
                    continue;
                }

                if (threshold != null && value != null) {
                    boolean isEvent = value < threshold.getMinValue() || value > threshold.getMaxValue();
                    setValueEventByNo(dto, i, isEvent);
                }
            }

            sensorDtoList.add(dto);
        }

        return sensorDtoList;
    }


    private Double getValueByNo(Sensor sensor, int i) {
        switch (i) {
            case 1: return sensor.getValue1();
            case 2: return sensor.getValue2();
            case 3: return sensor.getValue3();
            case 4: return sensor.getValue4();
            case 5: return sensor.getValue5();
            case 6: return sensor.getValue6();
            case 7: return sensor.getValue7();
            case 8: return sensor.getValue8();
            case 9: return sensor.getValue9();
            case 10: return sensor.getValue10();
            default: return null;
        }
    }

    private void setValueEventByNo(SensorDto dto, int i, boolean isEvent) {
        switch (i) {
            case 1: dto.setValue1Event(isEvent); break;
            case 2: dto.setValue2Event(isEvent); break;
            case 3: dto.setValue3Event(isEvent); break;
            case 4: dto.setValue4Event(isEvent); break;
            case 5: dto.setValue5Event(isEvent); break;
            case 6: dto.setValue6Event(isEvent); break;
            case 7: dto.setValue7Event(isEvent); break;
            case 8: dto.setValue8Event(isEvent); break;
            case 9: dto.setValue9Event(isEvent); break;
            case 10: dto.setValue10Event(isEvent); break;
        }
    }


    // 회사별 최신 센서 데이터 조회
    public List<SensorDto> getLatestSensorDataDtoByCompany(Long companyId) {
        List<Sensor> sensorList = sensorRepository.getLatestSensorDataDtoByCompany(companyId);
        List<SensorDto> sensorDtoList = new ArrayList<>();

        for(Sensor sensor: sensorList) {
            SensorDto dto = SensorDto.createSensorDto(Optional.of(sensor));


            // 반복문으로 value1 ~ value10에 대해 이벤트 발생 여부 체크
            for (int i = 1; i <= 10; i++) {
                Double value = getValueByNo(sensor, i);

                // 최근 등록 임계값 조회
                SensorThreshold threshold = thresholdRepository
                        .findTopByCompanyIdAndDeviceCodeAndValueNoOrderByCreatedAtDesc(
                                sensor.getCompanyId(), sensor.getDeviceCode(), i);

                // 냉장고 온도(value 8~10)는 특수 예외 처리
                if ((i >= 8 && i <= 10) && (value == -242 || value == 988.7)) {
                    setValueEventByNo(dto, i, false);
                    continue;
                }

                if (threshold != null && value != null) {
                    boolean isEvent = value < threshold.getMinValue() || value > threshold.getMaxValue();
                    setValueEventByNo(dto, i, isEvent);
                }
            }

            sensorDtoList.add(dto);
        }

        return sensorDtoList;
    }
}