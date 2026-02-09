package com.example.dailyreport.repository;

import com.example.dailyreport.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    java.util.List<Notification> findByUserIdOrderBySentAtDesc(Long userId);
}
