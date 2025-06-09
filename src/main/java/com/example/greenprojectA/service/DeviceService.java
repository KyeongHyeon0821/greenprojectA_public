package com.example.greenprojectA.service;

import com.example.greenprojectA.dto.DeviceDto;
import com.example.greenprojectA.entity.Device;
import com.example.greenprojectA.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

  private final DeviceRepository deviceRepository;

  public List<DeviceDto> getDeviceList() {
    List<Device> deviceList = deviceRepository.findAllByOrderByIdxDesc();
    List<DeviceDto> deviceDtoList = new ArrayList<>();

    for (Device device : deviceList) {
      DeviceDto deviceDto = DeviceDto.createDeviceDto(Optional.of(device));
      deviceDtoList.add(deviceDto);
    }

    return deviceDtoList;
  }


  public DeviceDto getDevice(String deviceCode) {
    Device device = deviceRepository.findByDeviceCode(deviceCode);
    return DeviceDto.createDeviceDto(Optional.ofNullable(device));
  }

  public List<DeviceDto> getDeviceListByCompany(Long companyId) {
    List<Device> deviceList = deviceRepository.findByCompanyIdOrderByIdxDesc(companyId);
    List<DeviceDto> deviceDtoList = new ArrayList<>();

    for (Device device : deviceList) {
      DeviceDto deviceDto = DeviceDto.createDeviceDto(Optional.of(device));
      deviceDtoList.add(deviceDto);
    }

    return deviceDtoList;
  }
}
