package com.example.demo.modules.schemes.repository;

import com.example.demo.modules.schemes.entity.GovtScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GovtSchemeRepository extends JpaRepository<GovtScheme, Long> {
    List<GovtScheme> findByTargetStateIgnoreCaseOrTargetStateIgnoreCase(String state1, String state2);
}
