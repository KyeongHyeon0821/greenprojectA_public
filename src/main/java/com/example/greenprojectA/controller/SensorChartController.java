package com.example.greenprojectA.controller;

import com.example.greenprojectA.dto.SensorChartResponseDto;
import com.example.greenprojectA.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sensor-chart")
public class SensorChartController {

    private final SensorService sensorService;

    @GetMapping
    public List<SensorChartResponseDto> getSensorChartData(
            @RequestParam String deviceCode,
            @RequestParam String valueKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        return sensorService.getChartData(deviceCode, valueKey, start, end);
    }
}

