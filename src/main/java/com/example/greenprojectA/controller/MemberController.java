package com.example.greenprojectA.controller;

import com.example.greenprojectA.dto.MemberDto;
import com.example.greenprojectA.entity.Member;
import com.example.greenprojectA.service.MemberService;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.greenprojectA.constant.Role;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JavaMailSender mailSender;
    private final HttpSession session;
    private final PasswordEncoder passwordEncoder;

    // 로그인 페이지
    @GetMapping("/memberLogin")
    public String memberLoginGet(HttpServletRequest request, Model model, @ModelAttribute("message") String message) {

        // 쿠키에서 rememberId 꺼내기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("rememberId")) {
                    model.addAttribute("savedId", c.getValue());
                    break;
                }
            }
        }
        return "member/memberLogin";
    }

    // 로그인 실패 시
    @GetMapping("/login/error")
    public String loginError(HttpServletRequest request, RedirectAttributes rttr) {
        Exception exception = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        String customMessage = (String) request.getSession().getAttribute("customLoginError");

        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
        if (customMessage != null) {
            errorMessage = customMessage;
            request.getSession().removeAttribute("customLoginError");
        } else if (exception instanceof LockedException) {
            errorMessage = exception.getMessage();
        }

        rttr.addFlashAttribute("message", errorMessage);
        request.getSession().removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");

        return "redirect:/member/memberLogin";
    }

    // 로그인 성공 후
    @GetMapping("/memberLoginOk")
    public String memberLoginOkGet(Authentication authentication,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   RedirectAttributes rttr) {

        String mid = authentication.getName();
        Member member = memberService.findByMid(mid);

        Role role = member.getRole();

        if (role == Role.PENDING) {
            rttr.addFlashAttribute("message", "관리자의 승인이 필요합니다.");
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/member/memberLogin";
        }

        if (role == Role.WITHDRAWN) {
            rttr.addFlashAttribute("message", "탈퇴 요청 상태이므로 로그인할 수 없습니다.");
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/member/memberLogin";
        }

        HttpSession session = request.getSession();
        if (session.getAttribute("tempPwdUser") != null &&
                session.getAttribute("tempPwdUser").equals(mid)) {

            rttr.addFlashAttribute("message", "임시 비밀번호로 로그인되었습니다.\n보안을 위해 비밀번호를 변경해주세요.");
            session.removeAttribute("tempPwdUser");
            return "redirect:/";
        }

        rttr.addFlashAttribute("message", member.getUsername() + "님 로그인 되었습니다.");
        session.setAttribute("sName", member.getUsername());

        String strLevel = switch (role) {
            case ADMIN -> "관리자";
            case USER -> "기업회원";
            default -> "알 수 없음";
        };
        session.setAttribute("strLevel", strLevel);

        return "redirect:/";
    }

    // 로그아웃
    @GetMapping("/memberLogout")
    public String memberLogout(HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes rttr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String mid = authentication.getName();
            Member member = memberService.findByMid(mid);
            rttr.addFlashAttribute("message", member.getUsername() + "님 로그아웃 되었습니다.");
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/member/memberLogin";
    }

    // 회원가입 폼
    @GetMapping("/memberJoin")
    public String memberJoinForm(Model model) {
        MemberDto dto = new MemberDto();
        dto.setCompanyId(null);
        model.addAttribute("memberDto", dto);
        model.addAttribute("companyList", memberService.getCompanyList());
        return "member/memberJoin";
    }

    // 회원가입 처리
    @PostMapping("/memberJoin")
    public String memberJoinSubmit(@Valid @ModelAttribute MemberDto memberDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes rttr,
                                   Model model) {

        // 이메일 인증 확인
        Boolean isVerified = (Boolean) session.getAttribute("emailVerified");
        if (isVerified == null || !isVerified) {
            rttr.addFlashAttribute("message", "이메일 인증을 완료해주세요.");
            return "redirect:/member/memberJoin";
        }

        // 비밀번호 확인 검증
        if (!memberDto.isPasswordConfirmed()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "비밀번호가 일치하지 않습니다.");
            rttr.addFlashAttribute("message", "비밀번호가 일치하지 않습니다.");
            return "redirect:/member/memberJoin";
        }

        // 유효성 검사 실패
        if (bindingResult.hasErrors()) {
            model.addAttribute("companyList", memberService.getCompanyList());
            return "member/memberJoin";
        }

        try {
            memberService.registerMember(memberDto);
            session.removeAttribute("emailVerified");
            rttr.addFlashAttribute("message", memberDto.getUsername() + "님, 회원가입 요청이 완료되었습니다.\n관리자의 승인을 기다려주세요.");
            return "redirect:/member/memberLogin";
        } catch (IllegalStateException e) {
            e.printStackTrace();
            rttr.addFlashAttribute("message", "중복된 아이디 또는 이메일입니다.");
            return "redirect:/member/memberJoin";
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("message", "회원가입에 실패하였습니다. 관리자에게 문의해주세요.");
            return "redirect:/member/memberJoin";
        }
    }

    // 아이디 중복 체크
    @ResponseBody
    @GetMapping("/idCheck")
    public String idCheck(@RequestParam String mid) {
        return memberService.isMidExists(mid) ? "1" : "0";
    }

    // 탈퇴 요청 처리
    @PostMapping("/withdraw")
    public String withdrawMember(Authentication authentication,
                                 RedirectAttributes rttr) {
        String mid = authentication.getName();
        memberService.requestWithdrawal(mid);
        SecurityContextHolder.clearContext(); // 즉시 로그아웃
        rttr.addFlashAttribute("message", "탈퇴 요청이 완료되었습니다. 7일의 유예기간 후 완전히 삭제됩니다.");
        return "redirect:/";
    }

    // 이메일 인증번호 전송
    @PostMapping("/sendCode")
    @ResponseBody
    public String sendEmailCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        session.setAttribute("emailCode", code);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent =
                    "<div style='background-color:#f4f6f8; padding: 40px 0; text-align: center;'>" +
                            "<div style='display: inline-block; background: #fff; padding: 32px 24px; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); max-width: 480px; width: 90%;'>" +
                            "<img src='cid:logoImage' alt='EcoView 로고' style='width: 120px; margin-bottom: 24px;' />" +
                            "<h2 style='color: #2c3e50; margin-bottom: 16px;'>이메일 인증코드</h2>" +
                            "<p style='font-size: 16px; color: #333;'>안녕하세요, <strong style='color:#2c3e50;'>ecoview</strong>를 이용해주셔서 감사합니다.<br>아래 인증번호를 입력해 주세요.</p>" +
                            "<div style='margin: 20px auto; font-size: 26px; font-weight: bold; color: #1f2f3d; background: #ffffff; border: 2px dashed #2c3e50; padding: 16px; border-radius: 10px; letter-spacing: 3px; display: inline-block;'>" +
                            code + "</div>" +
                            "<p style='font-size: 14px; color: #666; margin-top: 24px;'>해당 인증번호는 보안상 <strong>5분 이내</strong>에 입력해 주세요.<br>타인에게 공유하지 마세요.</p>" +
                            "<hr style='margin: 32px 0; border: none; border-top: 1px solid #ccc;' />" +
                            "<p style='font-size: 12px; color: #999;'>ⓒ EcoView | 본 메일은 발신 전용입니다.</p>" +
                            "</div>" +
                            "</div>";

            helper.setTo(email);
            helper.setSubject("[EcoView] 이메일 인증코드");
            helper.setText(htmlContent, true);

            // 로고 삽입
            FileSystemResource logo = new FileSystemResource(new File("src/main/resources/static/images/logo.png"));
            helper.addInline("logoImage", logo);

            mailSender.send(message);
            return "ok";

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 인증번호 확인
    @PostMapping("/verifyCode")
    @ResponseBody
    public String verifyEmailCode(@RequestBody Map<String, String> payload) {
        String inputCode = payload.get("code");
        String savedCode = (String) session.getAttribute("emailCode");

        if (savedCode != null && savedCode.equals(inputCode)) {
            session.setAttribute("emailVerified", true);
            return "success";
        } else {
            return "fail";
        }
    }

    // 아이디 찾기 팝업
    @GetMapping("/findId")
    public String showFindIdForm() {
        return "member/findId"; // Thymeleaf 템플릿 경로
    }

    // 아이디 찾기 처리
    @PostMapping("/findId")
    public String findId(@RequestParam String username,
                         @RequestParam String email,
                         Model model) {

        Optional<Member> memberOpt = memberService.findByUsernameAndEmail(username, email);
        if (memberOpt.isPresent()) {
            model.addAttribute("foundId", memberOpt.get().getMid());
        } else {
            model.addAttribute("notFound", true);
            model.addAttribute("message", "일치하는 회원 정보가 없습니다.");
        }
        return "member/findId";
    }


    // 비밀번호 찾기 팝업
    @GetMapping("/findPwd")
    public String showFindPwdForm() {
        return "member/findPwd"; // Thymeleaf 템플릿 경로
    }

    // 비밀번호 찾기 처리
    @PostMapping("/findPwd")
    public String findPwd(@RequestParam String mid,
                          @RequestParam String email,
                          RedirectAttributes rttr) {

        Optional<Member> memberOpt = memberService.findByMidAndEmail(mid, email);

        if (memberOpt.isPresent()) {
            String tempPwd = UUID.randomUUID().toString().substring(0, 8); // 임시비번 8자리
            memberService.updatePassword(mid, tempPwd);

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                String htmlContent =
                        "<div style='background-color:#f4f6f8; padding: 40px 0; text-align: center;'>" +
                                "<div style='display: inline-block; background: #fff; padding: 32px 24px; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); max-width: 480px; width: 90%;'>" +
                                "<img src='cid:logoImage' alt='EcoView 로고' style='width: 120px; margin-bottom: 24px;' />" +
                                "<h2 style='color: #2c3e50; margin-bottom: 16px;'>임시 비밀번호 안내</h2>" +
                                "<p style='font-size: 16px; color: #333;'>요청하신 임시 비밀번호는 아래와 같습니다.</p>" +
                                "<div style='margin: 20px auto; font-size: 24px; font-weight: bold; color: #1f2f3d; background: #ffffff; border: 2px dashed #2c3e50; padding: 16px; border-radius: 10px; letter-spacing: 2px; display: inline-block;'>" +
                                tempPwd + "</div>" +
                                "<p style='font-size: 14px; color: #666; margin-top: 24px;'>로그인 후 반드시 <strong>비밀번호를 변경</strong>해 주세요.</p>" +
                                "<hr style='margin: 32px 0; border: none; border-top: 1px solid #ccc;' />" +
                                "<p style='font-size: 12px; color: #999;'>ⓒ EcoView | 본 메일은 발신 전용입니다.</p>" +
                                "</div>" +
                                "</div>";

                helper.setTo(email);
                helper.setSubject("[EcoView] 임시 비밀번호 안내");
                helper.setText(htmlContent, true);

                // 로고 이미지 삽입
                FileSystemResource logo = new FileSystemResource(new File("src/main/resources/static/images/logo.png"));
                helper.addInline("logoImage", logo);

                mailSender.send(message);

                session.setAttribute("tempPwdUser", mid);
                rttr.addFlashAttribute("message", "임시 비밀번호가 이메일로 발송되었습니다.\n보안을 위해 로그인 후 비밀번호를 변경해주세요.");
                return "redirect:/member/memberLogin";

            } catch (Exception e) {
                e.printStackTrace();
                rttr.addFlashAttribute("message", "이메일 전송에 실패했습니다.");
                return "redirect:/member/findPwd";
            }

        } else {
            rttr.addFlashAttribute("message", "일치하는 회원 정보가 없습니다.");
            return "redirect:/member/findPwd";
        }
    }

    // 마이페이지 메뉴
    @GetMapping("/mypage")
    public String showMyPage() {
        return "member/myPage";
    }

    // 비밀번호 확인폼
    @GetMapping("/mypage/checkPwd")
    public String showPwdCheckForm() {
        return "member/pwdCheck";
    }

    @PostMapping("/mypage/checkPwd")
    public String checkPassword(@RequestParam String password,
                                HttpSession session,
                                RedirectAttributes rttr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String mid = auth.getName();

        Member member = memberService.findByMid(mid);
        if (!passwordEncoder.matches(password, member.getPassword())) {
            rttr.addFlashAttribute("message", "비밀번호가 일치하지 않습니다.");
            return "redirect:/member/mypage/checkPwd";
        }

        session.setAttribute("mypageVerified", true);
        return "redirect:/member/mypage/info";
    }

    // 마이페이지 - 회원정보 수정 페이지
    @GetMapping("/mypage/info")
    public String showInfoUpdatePage(HttpSession session, RedirectAttributes rttr, Model model) {
        Boolean verified = (Boolean) session.getAttribute("mypageVerified");
        if (verified == null || !verified) {
            rttr.addFlashAttribute("message", "회원정보 수정을 위해 비밀번호를 먼저 확인해주세요.");
            return "redirect:/member/mypage/checkPwd";
        }
        String mid = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberService.findByMid(mid);

        model.addAttribute("member", member);

        return "member/infoUpdate";
    }

    // 회원정보 수정
    @PostMapping("/memberUpdate")
    public String updateMember(@ModelAttribute MemberDto memberDto,
                               BindingResult bindingResult,
                               RedirectAttributes rttr,
                               HttpSession session,
                               Model model) {

        // 유효성 검사 실패 시 다시 폼으로
        if (bindingResult.hasErrors()) {
            model.addAttribute("companyList", memberService.getCompanyList());
            return "member/inforUpdate";
        }

        try {
            memberService.updateMember(memberDto);
            rttr.addFlashAttribute("message", "회원정보가 수정되었습니다.");
            return "redirect:/member/mypage";
        } catch (Exception e) {
            rttr.addFlashAttribute("message", "수정 중 오류가 발생했습니다.");
            return "redirect:/member/inforUpdate";
        }
    }

    // 비밀번호 변경폼
    @GetMapping("/mypage/changePwd")
    public String showChangePwdPage() {
        return "member/pwdChange";
    }

    // 비밀번호 변경
    @PostMapping("/mypage/changePwd")
    public String changePassword(@RequestParam String currentPwd,
                                 @RequestParam String newPwd,
                                 @RequestParam String confirmPwd,
                                 Authentication authentication,
                                 RedirectAttributes rttr) {

        String mid = authentication.getName();
        Member member = memberService.findByMid(mid);

        if (!passwordEncoder.matches(currentPwd, member.getPassword())) {
            rttr.addFlashAttribute("message", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/member/mypage/changePwd";
        }
        if (!newPwd.equals(confirmPwd)) {
            rttr.addFlashAttribute("message", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/member/mypage/changePwd";
        }
        if (passwordEncoder.matches(newPwd, member.getPassword())) {
            rttr.addFlashAttribute("message", "이전과 동일한 비밀번호는 사용할 수 없습니다.");
            return "redirect:/member/mypage/changePwd";
        }

        memberService.updatePassword(mid, newPwd);
        rttr.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
        return "redirect:/member/memberLogin";
    }
}