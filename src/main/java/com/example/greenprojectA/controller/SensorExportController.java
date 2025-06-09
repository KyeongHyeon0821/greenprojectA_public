package com.example.greenprojectA.controller;

import com.example.greenprojectA.entity.Sensor;
import com.example.greenprojectA.service.SensorExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sensor/export-page")
public class SensorExportController {

    private final SensorExportService sensorExportService;

    @GetMapping("/export")
    public ResponseEntity<Resource> exportSensorToExcel(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String deviceCode) {
        return sensorExportService.exportToExcel(startDate, endDate, deviceCode);
    }

    @GetMapping("/preview")
    public List<Sensor> previewSensorData(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String deviceCode) {
        return sensorExportService.getSensorData(startDate, endDate, deviceCode);
    }


}
