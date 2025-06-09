package com.example.greenprojectA.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SensorChartViewController {
    @GetMapping("/sensor-chart")
    public String showSensorChartPage() {
        return "chart/chart"; // resources/templates/chart/chart.html
    }
}