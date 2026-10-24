package com.example.demo.modules.finance.repository;

import com.example.demo.modules.finance.entity.FarmFinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmFinancialTransactionRepository extends JpaRepository<FarmFinancialTransaction, Long> {
    List<FarmFinancialTransaction> findByFarmerIdOrderByTransactionDateDesc(Long farmerId);
}
