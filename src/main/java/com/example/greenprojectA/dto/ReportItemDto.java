package com.example.greenprojectA.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportItemDto {
    private int no;
    private Integer sensorId;
    private String deviceCode;
    private int valueNo;
    private String itemName;

    private double min;
    private double avg;
    private double max;

    private double prevMinAvg;   // 이전 기간 최소값 평균
    private double prevAvg;      // 이전 기간 평균값 평균
    private double prevMaxAvg;   // 이전 기간 최대값 평균

    private double minChangePercent;
    private double avgChangePercent;
    private double maxChangePercent;

    private int eventCount;

    private String valueField;

    private String unit; // 예: ℃, %, ppm, ㎍/㎥ 등
}
