package com.example.greenprojectA.config;

import com.example.greenprojectA.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.example.greenprojectA.config.LoginFailHandler;
import com.example.greenprojectA.config.LoginSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final LoginFailHandler customAuthenticationFailureHandler;
  private final LoginSuccessHandler customAuthenticationSuccessHandler;

  public SecurityConfig(LoginFailHandler customAuthenticationFailureHandler,
                        LoginSuccessHandler customAuthenticationSuccessHandler) {
    this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    // CSRF 토큰을 요청 속성에 설정하는 핸들러 생성
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName("_csrf");

    // 사용자가 만든 로그인폼 적용하기
    http
            .csrf(csrf -> csrf
                    // CSRF 토큰 요청 핸들러 설정
                    .csrfTokenRequestHandler(requestHandler)
                    // /register 경로에 대해 CSRF 보호 비활성화
                    .ignoringRequestMatchers("/member/**")
                    // CSRF 토큰을 쿠키로 저장, HttpOnly 설정 비활성화
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .formLogin(form -> form
            .loginPage("/member/memberLogin")
            .defaultSuccessUrl("/member/memberLoginOk", true)
            .failureHandler(customAuthenticationFailureHandler)
            .successHandler(customAuthenticationSuccessHandler)
            .usernameParameter("mid")
            .permitAll()
            );

    // 요청 URL별 접근 권한 설정
    http.authorizeHttpRequests(request -> request
            .requestMatchers("/", "/index", "/home", "/h").permitAll()
            .requestMatchers("/css/**", "/images/**", "/guest/**").permitAll()
            .requestMatchers("/member/idCheck").permitAll()
            .requestMatchers("/member/memberLogin", "/member/memberLoginOk", "/member/login/error", "/member/memberJoin").permitAll()
            .requestMatchers("/member/findId", "/member/findPwd").permitAll()
            .requestMatchers("/member/sendCode", "/member/verifyCode").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/biz/**").hasRole("USER")
            .requestMatchers("/member/memberMain").authenticated()
            .anyRequest().authenticated()
    );

    // 권한 예외 처리
    http.exceptionHandling(exception -> exception
            .accessDeniedPage("/error/accessDenied")
    );

    // 로그아웃 기본 처리
    http.logout(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
