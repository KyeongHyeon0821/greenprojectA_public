package com.example.greenprojectA.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEventLogDto {
    private Integer eventLogId;
    private Integer companyId;
    private String deviceCode;
    private Integer valueNo;
    private Double measuredValue;
    private Double minValue;
    private Double maxValue;
    private LocalDateTime measureDatetime;
    private LocalDateTime eventDatetime;
    private String description;
}
