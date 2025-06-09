package com.example.greenprojectA.service;

import com.example.greenprojectA.dto.ReportItemDto;
import com.example.greenprojectA.entity.Sensor;
import com.example.greenprojectA.repository.EventLogRepository;
import com.example.greenprojectA.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorReportService {

    private final SensorRepository sensorRepository;
    private final EventLogRepository eventLogRepository;

    private static final Map<Integer, String> valueNames = Map.ofEntries(
            Map.entry(1, "실내온도"), Map.entry(2, "상대습도"), Map.entry(3, "이산화탄소"),
            Map.entry(4, "유기화합물VOC"), Map.entry(5, "초미세먼지 PM1.0"),
            Map.entry(6, "초미세먼지 PM2.5"), Map.entry(7, "초미세먼지 PM10"),
            Map.entry(8, "온도_1"), Map.entry(9, "온도_2"), Map.entry(10, "온도_3"),
            Map.entry(11, "소음"), Map.entry(12, "온도(비접촉)"), Map.entry(13, "조도")
    );

    // value 번호별 단위 매핑
    private static final Map<Integer, String> valueUnits = Map.ofEntries(
            Map.entry(1, "℃"),
            Map.entry(2, "%"),
            Map.entry(3, "ppm"),
            Map.entry(4, "ppb"),
            Map.entry(5, "㎍/㎥"),
            Map.entry(6, "㎍/㎥"),
            Map.entry(7, "㎍/㎥"),
            Map.entry(8, "℃"),
            Map.entry(9, "℃"),
            Map.entry(10, "℃"),
            Map.entry(11, "dB"),
            Map.entry(12, "℃"),
            Map.entry(13, "Lux")
    );

    private Double getValueByNo(Sensor sensor, int valueNo) {
        return switch (valueNo) {
            case 1 -> sensor.getValue1();
            case 2 -> sensor.getValue2();
            case 3 -> sensor.getValue3();
            case 4 -> sensor.getValue4();
            case 5 -> sensor.getValue5();
            case 6 -> sensor.getValue6();
            case 7 -> sensor.getValue7();
            case 8 -> sensor.getValue8();
            case 9 -> sensor.getValue9();
            case 10 -> sensor.getValue10();
            case 11 -> sensor.getValue11();
            case 12 -> sensor.getValue12();
            case 13 -> sensor.getValue13();
            default -> null;
        };
    }

    public List<ReportItemDto> generateReport(String deviceCode, LocalDate baseDate, String reportType) {

        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (reportType.toLowerCase()) {
            case "week" -> {
                startDate = baseDate.with(DayOfWeek.MONDAY).atStartOfDay();
                endDate = startDate.plusDays(7).minusSeconds(1);
            }
            case "month" -> {
                startDate = baseDate.withDayOfMonth(1).atStartOfDay();
                endDate = startDate.plusMonths(1).minusSeconds(1);
            }
            default -> { // day
                startDate = baseDate.atStartOfDay();
                endDate = baseDate.atTime(LocalTime.MAX);
            }
        }

        LocalDateTime prevStart;
        LocalDateTime prevEnd;

        switch (reportType.toLowerCase()) {
            case "week" -> {
                prevStart = startDate.minusWeeks(1);
                prevEnd = endDate.minusWeeks(1);
            }
            case "month" -> {
                prevStart = startDate.minusMonths(1);
                prevEnd = endDate.minusMonths(1);
            }
            default -> {
                prevStart = startDate.minusDays(1);
                prevEnd = startDate.minusSeconds(1);
            }
        }

        List<Sensor> currentSensors = sensorRepository.findByDeviceCodeAndDateRange(deviceCode, startDate, endDate);
        List<Sensor> prevSensors = sensorRepository.findByDeviceCodeAndDateRange(deviceCode, prevStart, prevEnd);

        List<ReportItemDto> result = new ArrayList<>();

        for (int i = 1; i <= 13; i++) {
            final int valueNo = i;

            List<Double> currentValues = currentSensors.stream()
                    .map(sensor -> getValueByNo(sensor, valueNo))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<Double> prevValues = prevSensors.stream()
                    .map(sensor -> getValueByNo(sensor, valueNo))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (currentValues.isEmpty()) continue;

            double min = Collections.min(currentValues);
            double max = Collections.max(currentValues);
            double avg = currentValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            double prevMinAvg = prevValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double prevMaxAvg = prevValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double prevAvg = prevValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            double minChangePercent = 0.0;
            double avgChangePercent = 0.0;
            double maxChangePercent = 0.0;

            if (prevMinAvg != 0.0) {
                minChangePercent = ((min - prevMinAvg) / prevMinAvg) * 100.0;
            }
            if (prevAvg != 0.0) {
                avgChangePercent = ((avg - prevAvg) / prevAvg) * 100.0;
            }
            if (prevMaxAvg != 0.0) {
                maxChangePercent = ((max - prevMaxAvg) / prevMaxAvg) * 100.0;
            }

            // 소수점 둘째자리까지 반올림
            minChangePercent = Math.round(minChangePercent * 100.0) / 100.0;
            avgChangePercent = Math.round(avgChangePercent * 100.0) / 100.0;
            maxChangePercent = Math.round(maxChangePercent * 100.0) / 100.0;

            int eventCount = eventLogRepository.countByDeviceAndValueNoAndRange(deviceCode, valueNo, startDate, endDate);

            Integer sensorId = currentSensors.isEmpty() ? null : currentSensors.get(0).getSensorId();

            result.add(ReportItemDto.builder()
                    .no(valueNo)
                    .sensorId(sensorId)
                    .deviceCode(deviceCode)
                    .valueNo(valueNo)
                    .itemName(valueNames.get(valueNo))
                    .min(min)
                    .avg(avg)
                    .max(max)
                    .prevMinAvg(prevMinAvg)
                    .prevAvg(prevAvg)
                    .prevMaxAvg(prevMaxAvg)
                    .minChangePercent(minChangePercent)
                    .avgChangePercent(avgChangePercent)
                    .maxChangePercent(maxChangePercent)
                    .eventCount(eventCount)
                    .valueField("value" + valueNo)
                    .unit(valueUnits.get(valueNo))   // 단위
                    .build());
        }

        return result;
    }
}
