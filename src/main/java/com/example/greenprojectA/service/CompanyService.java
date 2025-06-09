package com.example.greenprojectA.service;

import com.example.greenprojectA.entity.Company;
import com.example.greenprojectA.repository.CompanyRepository;
import com.example.greenprojectA.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;

    // 전체 기업 조회
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    // 기업 검색
    public List<Company> searchByName(String keyword) {
        return companyRepository.findByNameContaining(keyword);
    }

    // 기업 추가
    public void addCompany(Company company) {
        companyRepository.save(company);
    }

    // 기업 수정
    public void updateCompany(Company company) {
        Company existing = companyRepository.findById(company.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("기업 정보가 존재하지 않습니다."));

        existing.setName(company.getName());
        existing.setLocation(company.getLocation());

        companyRepository.save(existing);
    }

    // 기업 삭제
    public void deleteCompany(Long id) {
        if (memberRepository.existsByCompany_Idx(id)) {
            throw new IllegalStateException("소속된 회원이 있는 기업은 삭제할 수 없습니다.");
        }
        companyRepository.deleteById(id);
    }

    // (선택) 이름 중복 체크
    public boolean isNameExists(String name) {
        return companyRepository.existsByName(name);
    }
}