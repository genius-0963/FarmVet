package com.example.demo.modules.livestock.repository;

import com.example.demo.modules.livestock.entity.LivestockProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivestockProfileRepository extends JpaRepository<LivestockProfile, Long> {
    List<LivestockProfile> findByFarmerId(Long farmerId);
    Optional<LivestockProfile> findByTagId(String tagId);
}
