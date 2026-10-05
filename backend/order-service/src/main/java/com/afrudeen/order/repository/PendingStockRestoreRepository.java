package com.afrudeen.order.repository;

import com.afrudeen.order.entity.PendingStockRestore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingStockRestoreRepository extends JpaRepository<PendingStockRestore, Long> {

    List<PendingStockRestore> findTop50ByDoneFalseAndAttemptsLessThanOrderByIdAsc(int maxAttempts);
}