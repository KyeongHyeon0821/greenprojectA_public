package com.example.greenprojectA.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sensor/export-page")
public class SensorExportViewController {

    @GetMapping
    public String showExportPage() {
        return "chart/export";
    }
}