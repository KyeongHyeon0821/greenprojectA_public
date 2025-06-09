package com.example.greenprojectA.controller;

import com.example.greenprojectA.constant.Role;
import com.example.greenprojectA.dto.DeviceDto;
import com.example.greenprojectA.dto.SensorDto;
import com.example.greenprojectA.dto.SensorThresholdDto;
import com.example.greenprojectA.entity.Member;
import com.example.greenprojectA.entity.Company;
import com.example.greenprojectA.entity.SensorThreshold;
import com.example.greenprojectA.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;
    private final CompanyService companyService;
    private final SensorService sensorService;
    private final DeviceService deviceService;
    private final ThresholdService thresholdService;

    // 회원 리스트 + 검색 + Role 필터링
    @GetMapping("/member")
    public String showMemberList(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) String role,
                                 Model model) {

        List<Member> memberList;

        if (role != null && !role.isEmpty()) {
            memberList = memberService.findByRoleFiltered(role, category, keyword);
        } else if (keyword != null && !keyword.isEmpty() && category != null) {
            memberList = memberService.searchByCategory(category, keyword);
        } else {
            memberList = memberService.findAll();
        }

        model.addAttribute("memberList", memberList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("role", role);

        return "admin/memberList";
    }

    // 회원가입 승인 (PENDING → USER)
    @PostMapping("/approve")
    public String approveMember(@RequestParam Long memberId, RedirectAttributes rttr) {
        memberService.changeRole(memberId, Role.USER);
        rttr.addFlashAttribute("message", "가입 승인되었습니다.");
        return "redirect:/admin/member";
    }

    // 회원 승인 일괄 처리
    @PostMapping("bulkAction")
    public String handleBulkAction(@RequestParam List<Long> memberIds,
                                   @RequestParam String action,
                                   RedirectAttributes redirectAttributes) {

        if ("approve".equals(action)) {
            memberService.approveMembers(memberIds);
            redirectAttributes.addFlashAttribute("message", "선택된 회원의 가입 승인이 완료되었습니다.");
        }
        return "redirect:/admin/member";
    }

    // 기업 리스트 + 등록
    @GetMapping("/company")
    public String showCompanyPage(Model model) {
        model.addAttribute("company", new Company());
        model.addAttribute("companyList", companyService.findAll());
        return "admin/company";
    }

    // 기업 이름으로 검색
    @GetMapping("/admin/company")
    public String companyList(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Company> companyList;

        if (keyword != null && !keyword.trim().isEmpty()) {
            companyList = companyService.searchByName(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            companyList = companyService.findAll();
        }

        model.addAttribute("companyList", companyList);
        model.addAttribute("company", new Company());
        return "admin/company";
    }

    // 기업 등록, 수정 처리
    @PostMapping("/company")
    public String saveOrUpdateCompany(@ModelAttribute Company company, RedirectAttributes rttr) {
        if (company.getIdx() == null) {
            companyService.addCompany(company);
            rttr.addFlashAttribute("message", "기업이 등록되었습니다.");
        } else {
            companyService.updateCompany(company);
            rttr.addFlashAttribute("message", "기업 정보가 수정되었습니다.");
        }
        return "redirect:/admin/company";
    }

    // 기업 삭제 처리
    @PostMapping("/company/delete")
    public String deleteCompany(@RequestParam Long id, RedirectAttributes rttr) {
        try {
            companyService.deleteCompany(id);
            rttr.addFlashAttribute("message", "기업이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/admin/company";
    }

    @GetMapping("/dashboard")
    public String myDashBoardGet(Authentication authentication, Model model) {
        String mid = authentication.getName();
        Member member = memberService.findByMid(mid);

        List<DeviceDto> deviceDtoList = deviceService.getDeviceList();

        model.addAttribute("company", member.getCompany());
        model.addAttribute("deviceList", deviceDtoList);

        return "admin/dashboard";
    }

    @GetMapping("/getLatestData")
    @ResponseBody
    public List<SensorDto> getLatestData() {
        List<SensorDto> latestData = sensorService.getLatestSensorDataDto();
        return latestData;
    }

    @GetMapping("/deviceList")
    public String deviceListGet(Model model) {
        List<DeviceDto> deviceList = deviceService.getDeviceList();
        model.addAttribute("deviceList", deviceList);
        return "admin/deviceList";
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

        return "admin/thresholdUpdate";
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

        return "redirect:/admin/thresholdUpdate/" + thresholdDto.getDeviceCode();
    }


}