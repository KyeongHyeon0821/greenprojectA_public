package com.example.greenprojectA.entity;

import com.example.greenprojectA.dto.DeviceDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "device")
public class Device {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer idx;

  @Column(unique = true, nullable = false)
  private String deviceCode;  // 디바이스 코드

  private String deviceLocation; // 디바이스 위치

  private Integer companyId;  // 회사 ID

  // Dto to Entity
  public static Device createDevice(DeviceDto deviceDto) {
    return Device.builder()
            .idx(deviceDto.getIdx())
            .deviceCode(deviceDto.getDeviceCode())
            .deviceLocation(deviceDto.getDeviceLocation())
            .companyId(deviceDto.getCompanyId())
            .build();
  }
}
