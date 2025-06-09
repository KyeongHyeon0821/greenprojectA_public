package com.example.greenprojectA.service;

import com.example.greenprojectA.config.CustomUserDetails;
import com.example.greenprojectA.constant.Role;
import com.example.greenprojectA.dto.MemberDto;
import com.example.greenprojectA.entity.Company;
import com.example.greenprojectA.entity.Member;
import com.example.greenprojectA.repository.CompanyRepository;
import com.example.greenprojectA.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {

  private final MemberRepository memberRepository;
  private final CompanyRepository companyRepository;
  private final PasswordEncoder passwordEncoder;

  // 아이디 중복 확인
  public boolean isMidExists(String mid) {
    return memberRepository.existsByMid(mid);
  }

  // 회원가입 처리
  public void registerMember(MemberDto dto) {
    if (memberRepository.existsByMid(dto.getMid())) {
      throw new IllegalStateException("이미 사용 중인 아이디입니다.");
    }

    Company company = null;
    if (dto.getCompanyId() != null) {
      company = companyRepository.findById(dto.getCompanyId())
              .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 회사 ID입니다."));
    }

    Member member = Member.builder()
            .mid(dto.getMid())
            .username(dto.getUsername())
            .password(passwordEncoder.encode(dto.getPassword()))
            .email(dto.getEmail())
            .tel(dto.getTel())
            .address(dto.getAddress())
            .company(company)
            .role(Role.PENDING)  // 가입대기 상태
            .build();

    memberRepository.save(member);
  }

  // 로그인 성공 시 사용자 정보 조회
  public Member findByMid(String mid) {
    return memberRepository.findByMid(mid)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
  }

  // 로그인 실패 처리
  @Override
  public UserDetails loadUserByUsername(String mid) throws UsernameNotFoundException {
    Member member = memberRepository.findByMid(mid)
            .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다."));

    return new CustomUserDetails(member);
  }

  // 아이디 찾기
  public Optional<Member> findByUsernameAndEmail(String username, String email) {
    return memberRepository.findByUsernameAndEmail(username, email);
  }

  // 비밀번호 재설정 - 아이디 + 이메일 확인
  public Optional<Member> findByMidAndEmail(String mid, String email) {
    return memberRepository.findByMidAndEmail(mid, email);
  }

  // 비밀번호 변경
  public void updatePassword(String mid, String newPwd) {
    Member member = memberRepository.findByMid(mid)
            .orElseThrow(() -> new IllegalArgumentException("해당 아이디 없음"));
    member.setPassword(passwordEncoder.encode(newPwd));
    memberRepository.save(member);
  }

  // 기업 목록 조회 (회원가입용 드롭다운)
  public List<Company> getCompanyList() {
    return companyRepository.findAll();
  }

  // 전체 회원 조회 (관리자용)
  public List<Member> findAll() {
    return memberRepository.findAll();
  }

  // 회원 Role 필터링
  public List<Member> findByRoleFiltered(String roleStr, String category, String keyword) {
    Role role = Role.valueOf(roleStr);

    if (category != null && keyword != null && !keyword.isEmpty()) {
      return switch (category) {
        case "mid" -> memberRepository.findByRoleAndMidContaining(role, keyword);
        case "username" -> memberRepository.findByRoleAndUsernameContaining(role, keyword);
        case "company" -> memberRepository.findByRoleAndCompany_NameContaining(role, keyword);
        default -> memberRepository.findByRole(role);
      };
    } else {
      return memberRepository.findByRole(role);
    }
  }

  // 아이디, 이름, 기업명으로 검색
  public List<Member> searchByCategory(String category, String keyword) {
    return switch (category) {
      case "mid" -> memberRepository.findByMidContaining(keyword);
      case "username" -> memberRepository.findByUsernameContaining(keyword);
      case "company" -> memberRepository.findByCompany_NameContaining(keyword);
      default -> findAll();
    };
  }

  // 회원 일괄 승인
  public void approveMembers(List<Long> memberIds) {
    List<Member> members = memberRepository.findAllById(memberIds);
    for (Member m : members) {
      if (m.getRole() == Role.PENDING) {
        m.setRole(Role.USER);
      }
    }
    memberRepository.saveAll(members);
  }

  // 가입대기 회원만 조회 (필요 시)
  public List<Member> getPendingMembers() {
    return memberRepository.findByRole(Role.PENDING);
  }

  // 관리자 승인 처리 (PENDING → USER)
  public void approveMember(Long memberId) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    member.setRole(Role.USER);
    memberRepository.save(member);
  }

  // 역할(Role) 변경 (관리자에서 직접 지정)
  public void changeRole(Long memberId, Role newRole) {
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    member.setRole(newRole);
    memberRepository.save(member);
  }

  // 회원 정보 수정 처리
  public void updateMember(MemberDto dto) {
    Member member = memberRepository.findById(dto.getIdx())
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

    member.setEmail(dto.getEmail());
    member.setTel(dto.getTel());
    member.setAddress(dto.getAddress());

    memberRepository.save(member);
  }

  // 탈퇴 요청 처리 (사용자 측)
  public void requestWithdrawal(String mid) {
    Member member = memberRepository.findByMid(mid)
            .orElseThrow(() -> new UsernameNotFoundException("회원 정보가 존재하지 않습니다."));
    member.setRole(Role.WITHDRAWN);
    member.setQuitRequestedAt(LocalDateTime.now());
    memberRepository.save(member);
  }

  // 탈퇴 요청 회원 자동 삭제 (매일 새벽 3시 실행)
  @Scheduled(cron = "0 0 3 * * *")
  public void autoDeleteWithdrawnMembers() {
    List<Member> all = memberRepository.findAll();

    List<Member> toDelete = all.stream()
            .filter(m -> m.getRole() == Role.WITHDRAWN)
            .filter(m -> m.getQuitRequestedAt() != null)
            .filter(m -> ChronoUnit.DAYS.between(m.getQuitRequestedAt().toLocalDate(), LocalDate.now()) >= 7)
            .toList();

    memberRepository.deleteAll(toDelete);

    if (!toDelete.isEmpty()) {
      log.info("자동 삭제된 회원 수: {}", toDelete.size());
    }
  }
}