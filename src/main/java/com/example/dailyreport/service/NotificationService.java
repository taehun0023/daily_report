package com.example.dailyreport.service;

import com.example.dailyreport.domain.Notification;
import com.example.dailyreport.domain.RequestItem;
import com.example.dailyreport.repository.NotificationRepository;
import com.example.dailyreport.repository.RequestItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final RequestItemRepository requestRepository;

    public NotificationService(NotificationRepository notificationRepository, RequestItemRepository requestRepository) {
        this.notificationRepository = notificationRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public int notifyUsersForDate(LocalDate reportDate) {
        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();
        List<RequestItem> processed = requestRepository.findByProcessedAtBetween(start, end);

        Map<String, Long> countByUser = new HashMap<>();
        for (RequestItem item : processed) {
            countByUser.merge(item.getUserEmail(), 1L, Long::sum);
        }

        int created = 0;
        for (Map.Entry<String, Long> entry : countByUser.entrySet()) {
            Notification notification = new Notification();
            notification.setUserEmail(entry.getKey());
            notification.setMessage("전날 처리 완료 건수: " + entry.getValue() + "건");
            notificationRepository.save(notification);
            created++;
        }
        return created;
    }

    public List<Notification> findByUserEmail(String userEmail) {
        return notificationRepository.findByUserEmailOrderBySentAtDesc(userEmail);
    }
}
