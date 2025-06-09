package com.example.greenprojectA.config;

import com.example.greenprojectA.constant.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        String username = authentication.getName();

        // 사용자 Role 확인
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getMember().getRole(); // 예: "PENDING", "WITHDRAWN", "USER"

        // 승인대기나 탈퇴요청이면 강제 로그아웃 및 메시지 설정
        if (role == Role.PENDING) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            request.getSession().setAttribute("customLoginError", "관리자 승인 후 로그인 가능합니다.");
            response.sendRedirect("/member/login/error");
            return;
        }

        if (role == Role.WITHDRAWN) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            request.getSession().setAttribute("customLoginError", "탈퇴 요청된 계정입니다. 로그인할 수 없습니다.");
            response.sendRedirect("/member/login/error");
            return;
        }

        // 실패 카운트 초기화
        LoginFailHandler.failCountMap.remove(username);
        LoginFailHandler.lockUntilMap.remove(username);

        // 아이디 기억하기 쿠키 저장
        String remember = request.getParameter("rememberMe");
        if ("on".equals(remember)) {
            Cookie cookie = new Cookie("rememberId", username);
            cookie.setMaxAge(60 * 60 * 24 * 30); // 30일
            cookie.setPath("/");
            response.addCookie(cookie);
        } else {
            Cookie cookie = new Cookie("rememberId", null);
            cookie.setMaxAge(0); // 즉시 삭제
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        response.sendRedirect("/member/memberLoginOk");
    }
}