package com.example.dailyreport.service;

import com.example.dailyreport.domain.Notification;
import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.repository.NotificationRepository;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.repository.UserAccountRepository;
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
    private final SupportTicketRepository ticketRepository;
    private final UserAccountRepository userRepository;
    private final MailService mailService;

    public NotificationService(NotificationRepository notificationRepository,
                               SupportTicketRepository ticketRepository,
                               UserAccountRepository userRepository,
                               MailService mailService) {
        this.notificationRepository = notificationRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Transactional
    public int notifyUsersForDate(LocalDate reportDate) {
        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();
        List<SupportTicket> processed = ticketRepository.findByProcessedAtBetween(start, end);

        Map<Long, Long> countByUserId = new HashMap<>();
        for (SupportTicket item : processed) {
            if (item.getUser() != null && item.getUser().getId() != null) {
                countByUserId.merge(item.getUser().getId(), 1L, Long::sum);
            }
        }

        int created = 0;
        for (Map.Entry<Long, Long> entry : countByUserId.entrySet()) {
            Notification notification = new Notification();
            var user = userRepository.findById(entry.getKey()).orElse(null);
            if (user == null) {
                continue;
            }
            notification.setUser(user);
            notification.setMessage("전날 처리 완료 티켓 수: " + entry.getValue() + "건");
            notificationRepository.save(notification);
            mailService.send(user.getEmail(), "전날 처리 결과 알림", notification.getMessage());
            created++;
        }
        return created;
    }

    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }
}
