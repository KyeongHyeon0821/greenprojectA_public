package com.example.greenprojectA.dto;

import com.example.greenprojectA.entity.EventLog;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventLogDto {

    private Integer eventLogId; // 이벤트 로그 ID
    private Integer companyId; // 회사 ID
    private String deviceCode; // 디바이스 코드
    private Integer valueNo; // value1~13 중 몇 번째인지
    private Double measuredValue; // 센서에서 측정된 값
    private Double minValue; // 임계값 최소값
    private Double maxValue; // 임계값 최대값
    private LocalDateTime measureDatetime; // 센서 측정 일시
    private LocalDateTime eventDatetime; // 이벤트 발생 시각 (로그 기록 시각)
    private String description; // 이벤트 설명 (예: "임계값 초과")

    // Entity to Dto
    public static EventLogDto createEventLogDto(Optional<EventLog> optionalEventLog) {
        return EventLogDto.builder()
                .eventLogId(optionalEventLog.get().getEventLogId())
                .companyId(optionalEventLog.get().getCompanyId())
                .deviceCode(optionalEventLog.get().getDeviceCode())
                .valueNo(optionalEventLog.get().getValueNo())
                .measuredValue(optionalEventLog.get().getMeasuredValue())
                .minValue(optionalEventLog.get().getMinValue())
                .maxValue(optionalEventLog.get().getMaxValue())
                .measureDatetime(optionalEventLog.get().getMeasureDatetime())
                .eventDatetime(optionalEventLog.get().getEventDatetime())
                .description(optionalEventLog.get().getDescription())
                .build();
    }
}
