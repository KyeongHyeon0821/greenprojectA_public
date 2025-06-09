package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.greenprojectA.constant.Role;

import java.util.Optional;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByMid(String mid);
  Optional<Member> findByUsernameAndEmail(String username, String email);
  Optional<Member> findByMidAndEmail(String mid, String email);

  boolean existsByMid(String mid);
  boolean existsByEmail(String email);
  boolean existsByCompany_Idx(Long companyId);

  // 회원 검색
  List<Member> findByMidContaining(String mid);
  List<Member> findByUsernameContaining(String username);
  List<Member> findByCompany_NameContaining(String name);

  // 회원 Role 필터링
  List<Member> findByRoleAndMidContaining(Role role, String mid);
  List<Member> findByRoleAndUsernameContaining(Role role, String username);
  List<Member> findByRoleAndCompany_NameContaining(Role role, String name);

  // 가입대기 회원 조회
  List<Member> findByRole(Role role);
}
