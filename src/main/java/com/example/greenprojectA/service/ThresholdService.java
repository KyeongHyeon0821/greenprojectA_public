package com.example.greenprojectA.service;

import com.example.greenprojectA.dto.SensorThresholdDto;
import com.example.greenprojectA.entity.SensorThreshold;
import com.example.greenprojectA.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ThresholdService {

  private final ThresholdRepository thresholdRepository;

  public SensorThresholdDto getLatestThresholdByDeviceCode(String deviceCode, int i) {
    SensorThreshold sensorThreshold = thresholdRepository.findTopByDeviceCodeAndValueNoOrderByCreatedAtDesc(deviceCode, i);
    return SensorThresholdDto.createSensorThresholdDto(Optional.of(sensorThreshold));
  }

  public void setThresholdInput(SensorThreshold sensorThreshold) {
    thresholdRepository.save(sensorThreshold);
  }
}
