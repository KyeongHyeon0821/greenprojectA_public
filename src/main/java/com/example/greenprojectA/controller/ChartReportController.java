package com.example.greenprojectA.controller;

import com.example.greenprojectA.dto.ReportItemDto;
import com.example.greenprojectA.service.SensorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chart/report")
public class ChartReportController {

    private final SensorReportService sensorReportService;

    @GetMapping({"", "/", "/{reportType}"})
    public String showReportPage(
            @PathVariable(name = "reportType", required = false) String reportType,
            @RequestParam(name = "deviceCode", required = false, defaultValue = "ENV_V2_1") String deviceCode,
            @RequestParam(name = "baseDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate,
            Model model) {

        if (reportType == null) reportType = "day";

        boolean searched = baseDate != null; // ✅ 조회 여부 판단
        model.addAttribute("searched", searched);

        if (baseDate == null) {
            // 조회하지 않은 상태 — 폼만 보여주기
            model.addAttribute("reportItems", List.of()); // 빈 리스트로 설정
        } else {
            // 조회한 상태 — 리포트 생성
            List<ReportItemDto> reportItems = sensorReportService.generateReport(deviceCode, baseDate, reportType);
            model.addAttribute("reportItems", reportItems);
        }

        model.addAttribute("deviceCode", deviceCode);
        model.addAttribute("baseDate", baseDate != null ? baseDate : LocalDate.now());
        model.addAttribute("reportType", reportType);

        Map<String, String> deviceNameMap = Map.of(
                "ENV_V2_1", "1F",
                "ENV_V2_2", "2F",
                "ENV_V2_3", "자재실",
                "ENV_V2_4", "포장실"
        );
        model.addAttribute("deviceNameMap", deviceNameMap);

        return "chart/report";
    }
}
