package com.example.greenprojectA.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorChartResponseDto {
    private String label; // x축에 표시될 라벨 (ex. "2월 2일", "오후 3시")
    private Double value; // 센서 측정값
}