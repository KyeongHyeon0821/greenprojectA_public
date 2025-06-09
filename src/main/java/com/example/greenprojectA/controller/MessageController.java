package com.example.greenprojectA.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MessageController {

	@RequestMapping(value = "/message/{msgFlag}", method = RequestMethod.GET)
	public String getMessage(Model model, @PathVariable String msgFlag,
							 HttpSession session,
							 @RequestParam(name = "idx", defaultValue = "0", required = false) int idx
	) {

		if (msgFlag.equals("idCheckNo")) {
			model.addAttribute("message", "아이디가 중복되었습니다.\\n확인하시고 다시 입력하세요.");
			model.addAttribute("url", "member/memberJoin");

		} else if (msgFlag.equals("memberJoinOk")) {
			model.addAttribute("message", "회원가입 요청이 완료되었습니다.\\n관리자의 승인을 기다려주세요.");
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("memberJoinNo")) {
			model.addAttribute("message", "회원 가입에 실패하였습니다.\\n다시 회원가입 해주세요.");
			model.addAttribute("url", "member/memberJoin");

		} else if (msgFlag.equals("memberLoginOk")) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String mid = auth.getName();

			String msg = mid + "님 로그인 되었습니다!";
			model.addAttribute("message", msg);
			model.addAttribute("url", "member/memberMyPage");

		} else if (msgFlag.equals("memberLoginNo")) {
			model.addAttribute("message", "아이디 또는 비밀번호가 잘못 되었습니다.\\n아이디와 비밀번호를 정확히 입력해 주세요.");
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("memberLogoutOk")) {
			model.addAttribute("message", "로그아웃 되었습니다.");
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("pwdCheckNo")) {
			model.addAttribute("message", "비밀번호가 틀립니다. 다시 확인해 주세요.");
			model.addAttribute("url", "member/pwdCheck");

		} else if (msgFlag.equals("memberDeleteCheck")) {
			model.addAttribute("message", "탈퇴 요청이 완료되었습니다.\\n7일의 유예기간 후 완전히 삭제됩니다.");
			session.invalidate();
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("pwdChangeOk")) {
			model.addAttribute("message", "비밀번호 변경이 완료되었습니다.\\n다시 로그인해 주세요.");
			session.invalidate();
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("pwdChangeNo")) {
			model.addAttribute("message", "비밀번호 변경에 실패하였습니다.");
			model.addAttribute("url", "member/pwdCheck");

		} else if (msgFlag.equals("memberUpdateOk")) {
			model.addAttribute("message", "회원 정보 수정이 완료되었습니다.");
			model.addAttribute("url", "member/memberMyPage");

		} else if (msgFlag.equals("memberUpdateNo")) {
			model.addAttribute("message", "회원 정보 수정에 실패하였습니다.");
			model.addAttribute("url", "member/memberUpdate");

		} else if (msgFlag.equals("loginLockTimer")) {
			Long remaining = (Long) session.getAttribute("remainingTime");
			if (remaining == null) remaining = 60L;
			model.addAttribute("message", "로그인 실패 5회로 인해 로그인 제한 중입니다.\\n" + remaining + "초 후 다시 시도해주세요.");
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("memberFindPwdOk")) {
			model.addAttribute("message", "임시 비밀번호가 이메일로 전송되었습니다.");
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("memberFindPwdNo")) {
			model.addAttribute("message", "일치하는 회원 정보가 없습니다.");
			model.addAttribute("url", "member/memberFindPwd");

		} else if (msgFlag.equals("loginRequired")) {
			model.addAttribute("message", "로그인이 필요합니다.");
			model.addAttribute("url", "member/memberLogin");

		} else if (msgFlag.equals("levelError0")) {
			model.addAttribute("message", "관리자만 접근가능합니다.");
			model.addAttribute("url", "member/memberMain");
		}

		return "/include/message";
	}
}
