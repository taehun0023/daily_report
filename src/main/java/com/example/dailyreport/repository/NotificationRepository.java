package com.example.dailyreport.repository;

import com.example.dailyreport.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserEmailOrderBySentAtDesc(String userEmail);
}
