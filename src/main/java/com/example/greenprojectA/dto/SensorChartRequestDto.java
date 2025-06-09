package com.example.greenprojectA.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SensorChartRequestDto {
    private String deviceCode;
    private String valueKey;
    private LocalDate startDate;
    private LocalDate endDate;
}