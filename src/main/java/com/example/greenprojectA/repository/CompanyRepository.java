package com.example.greenprojectA.repository;

import com.example.greenprojectA.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByName(String name);

    // 기업이름 검색
    List<Company> findByNameContaining(String keyword);
}
