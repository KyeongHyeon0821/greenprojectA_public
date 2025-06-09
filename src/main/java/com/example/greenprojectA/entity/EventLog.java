package com.example.greenprojectA.entity;

import com.example.greenprojectA.dto.EventLogDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_log")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_log_id")
    private Integer eventLogId; // 이벤트 로그 ID

    private Integer companyId; // 회사 ID

    private String deviceCode; // 디바이스 코드

    private Integer valueNo; // value1~13 중 몇 번째인지

    private Double measuredValue; // 센서에서 측정된 값

    private Double minValue; // 임계값 최소값

    private Double maxValue; // 임계값 최대값

    private LocalDateTime measureDatetime; // 센서 측정 일시

    private LocalDateTime eventDatetime; // 이벤트 발생 시각 (로그 기록 시각)

    private String description; // 이벤트 설명 ("상한 임계값 초과, "하한 임계값 미만")

    // Dto to Entity
    public static EventLog createEventLog(EventLogDto eventLogDto) {
        return EventLog.builder()
                .companyId(eventLogDto.getCompanyId())
                .deviceCode(eventLogDto.getDeviceCode())
                .valueNo(eventLogDto.getValueNo())
                .measuredValue(eventLogDto.getMeasuredValue())
                .minValue(eventLogDto.getMinValue())
                .maxValue(eventLogDto.getMaxValue())
                .measureDatetime(eventLogDto.getMeasureDatetime())
                .eventDatetime(eventLogDto.getEventDatetime())
                .description(eventLogDto.getDescription())
                .build();
    }

}
