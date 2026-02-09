package com.example.dailyreport.repository;

import com.example.dailyreport.domain.RequestItem;
import com.example.dailyreport.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    long countByProcessedAtBetween(LocalDateTime start, LocalDateTime end);

    List<RequestItem> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    List<RequestItem> findByProcessedAtBetween(LocalDateTime start, LocalDateTime end);
}
