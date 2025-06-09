package com.example.greenprojectA.config;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginFailHandler extends SimpleUrlAuthenticationFailureHandler {

    public static final Map<String, Integer> failCountMap = new HashMap<>();
    public static final Map<String, Long> lockUntilMap = new HashMap<>();

    private static final int MAX_FAILS = 5;
    private static final long LOCK_DURATION_MS = 60 * 1000;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("mid");

        // 이미 LockedException이 던져진 상태라면, 아래 로직 무시하고 바로 메시지 유지
        if (!(exception instanceof LockedException)) {
            // 아직 잠긴 상태인가?
            if (lockUntilMap.containsKey(username)) {
                long lockedUntil = lockUntilMap.get(username);
                if (System.currentTimeMillis() < lockedUntil) {
                    exception = new LockedException("로그인 5회 실패로 1분간 제한됩니다.");
                } else {
                    // 제한 해제
                    lockUntilMap.remove(username);
                    failCountMap.remove(username);
                }
            }

            // 실패 횟수 증가
            int count = failCountMap.getOrDefault(username, 0) + 1;
            failCountMap.put(username, count);

            // 5회 초과 시 제한 걸기
            if (count >= MAX_FAILS) {
                lockUntilMap.put(username, System.currentTimeMillis() + LOCK_DURATION_MS);
            }
        }

        // 최종 예외 저장
        request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        setDefaultFailureUrl("/member/login/error");

        System.out.println("🔥 LoginFailHandler 최종 메시지 = " + exception.getMessage());

        super.onAuthenticationFailure(request, response, exception);
    }

}
