package com.example.greenprojectA.controller;

import com.example.greenprojectA.dto.DeviceDto;
import com.example.greenprojectA.dto.SensorDto;
import com.example.greenprojectA.dto.SensorThresholdDto;
import com.example.greenprojectA.entity.Member;
import com.example.greenprojectA.entity.SensorThreshold;
import com.example.greenprojectA.service.DeviceService;
import com.example.greenprojectA.service.MemberService;
import com.example.greenprojectA.service.SensorService;
import com.example.greenprojectA.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/biz")
@RequiredArgsConstructor
public class BizController {

  private final SensorService sensorService;

  private final MemberService memberService;

  private final DeviceService deviceService;

  private final ThresholdService thresholdService;

  @GetMapping("/bizMenu")
  public String bizMenuGet() {
    return "biz/bizMenu";
  }


  @GetMapping("/dashboard")
  public String myDashBoardGet(Authentication authentication, Model model) {
    String mid = authentication.getName();
    Member member = memberService.findByMid(mid);

    List<DeviceDto> deviceDtoList = deviceService.getDeviceListByCompany(member.getCompany().getIdx());

    model.addAttribute("company", member.getCompany());
    model.addAttribute("deviceList", deviceDtoList);

    return "biz/dashboard";
  }

  @GetMapping("/getLatestData")
  @ResponseBody
  public List<SensorDto> getLatestData(Authentication authentication) {
    String mid = authentication.getName();
    Member member = memberService.findByMid(mid);

    List<SensorDto> latestData = sensorService.getLatestSensorDataDtoByCompany(member.getCompany().getIdx());
    return latestData;
  }

  @GetMapping("/deviceList")
  public String deviceListGet(Model model, Authentication authentication) {
    String mid = authentication.getName();
    Member member = memberService.findByMid(mid);

    List<DeviceDto> deviceList = deviceService.getDeviceListByCompany(member.getCompany().getIdx());
    model.addAttribute("deviceList", deviceList);
    return "biz/deviceList";
  }

  @GetMapping("/thresholdUpdate/{deviceCode}")
  public String thresholdUpdateGet(@PathVariable String deviceCode, Model model) {
    DeviceDto deviceDto = deviceService.getDevice(deviceCode);

    List<SensorThresholdDto> thresholdDtoList = new ArrayList<>();
    // valueNo: 1~10 반복하며 최신값 가져오기
    for (int i = 1; i <= 10; i++) {
      SensorThresholdDto thresholdDto = thresholdService
              .getLatestThresholdByDeviceCode(deviceCode, i);
      if (thresholdDto != null) {
        thresholdDtoList.add(thresholdDto);
      }
    }

    model.addAttribute("deviceDto", deviceDto);
    model.addAttribute("thresholdList", thresholdDtoList);

    return "biz/thresholdUpdate";
  }

  @PostMapping("/thresholdUpdate/{deviceCode}/{valueNo}")
  public String updateThreshold(Authentication authentication, RedirectAttributes rttr, SensorThresholdDto thresholdDto) {
    String mid = authentication.getName();
    thresholdDto.setCreatedBy(mid); // 현재 로그인한 사용자의 ID 저장
    try {
      thresholdService.setThresholdInput(SensorThreshold.createSensorThreshold(thresholdDto));
      rttr.addFlashAttribute("message", "임계값이 성공적으로 저장되었습니다.");
    } catch (Exception e) {
      rttr.addFlashAttribute("message", "임계값 저장 중 오류가 발생했습니다.");
    }

    return "redirect:/biz/thresholdUpdate/" + thresholdDto.getDeviceCode();
  }


}
