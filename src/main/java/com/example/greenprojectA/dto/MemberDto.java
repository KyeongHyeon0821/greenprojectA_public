package com.example.greenprojectA.dto;

import com.example.greenprojectA.constant.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {

  @NotBlank(message = "아이디는 필수입니다.")
  @Pattern(regexp = "^[a-zA-Z0-9_-]{4,20}$", message = "아이디는 4~20자의 영문, 숫자, 특수문자(-,_)만 사용 가능합니다.")
  private String mid;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*]{4,16}$", message = "비밀번호는 4~16자의 영문, 숫자, 특수문자(!@#$%^&*)만 사용 가능합니다.")
  private String password;

  @NotBlank(message = "비밀번호 확인이 필요합니다.")
  private String confirmPassword;

  @NotBlank(message = "이름은 필수입니다.")
  private String username;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이어야 합니다.")
  private String email;

  // @NotNull(message = "소속 기업을 선택해야 합니다.")
  private Long companyId;

  private Long idx;
  private String tel;

  // 선택 입력 항목
  private String address;

  private Role role;

  // 커스텀 검증: 비밀번호와 확인 비밀번호 일치 여부
  public boolean isPasswordConfirmed() {
    return password != null && password.equals(confirmPassword);
  }
}
