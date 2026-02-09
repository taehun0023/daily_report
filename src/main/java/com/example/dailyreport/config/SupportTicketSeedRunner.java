package com.example.dailyreport.config;

import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketPriority;
import com.example.dailyreport.domain.TicketStatus;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SupportTicketSeedRunner implements CommandLineRunner {
    private final SupportTicketRepository ticketRepository;
    private final UserAccountRepository userRepository;

    public SupportTicketSeedRunner(SupportTicketRepository ticketRepository,
                                   UserAccountRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        LocalDate targetDate = LocalDate.of(2026, 2, 7);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        if (ticketRepository.countByCreatedAtBetween(start, end) > 0) {
            return;
        }

        List<UserAccount> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }
        UserAccount owner = users.get(0);

        SupportTicket t1 = baseTicket(owner, "로그인 오류가 발생합니다", "비밀번호 초기화 후에도 로그인 실패가 납니다.",
                "로그인", "계정", TicketPriority.HIGH, TicketStatus.RECEIVED, start.plusHours(9));
        SupportTicket t2 = baseTicket(owner, "결제 실패", "카드 결제 승인 오류가 반복됩니다.",
                "결제", "결제", TicketPriority.MEDIUM, TicketStatus.IN_PROGRESS, start.plusHours(11));
        SupportTicket t3 = baseTicket(owner, "환불 문의", "주문 취소 후 환불 일정 문의드립니다.",
                "환불", "서비스", TicketPriority.LOW, TicketStatus.DONE, start.plusHours(13));
        t3.setProcessedAt(start.plusHours(16));
        SupportTicket t4 = baseTicket(owner, "배송 지연", "배송 상태가 멈춰 있습니다.",
                "배송", "배송", TicketPriority.MEDIUM, TicketStatus.RECEIVED, start.plusHours(15));
        SupportTicket t5 = baseTicket(owner, "기능 개선 요청", "알림 이메일 템플릿 개선을 요청합니다.",
                "기타", "기술", TicketPriority.LOW, TicketStatus.RECEIVED, start.plusHours(18));

        ticketRepository.saveAll(List.of(t1, t2, t3, t4, t5));
    }

    private SupportTicket baseTicket(UserAccount owner,
                                     String title,
                                     String content,
                                     String inquiryType,
                                     String category,
                                     TicketPriority priority,
                                     TicketStatus status,
                                     LocalDateTime createdAt) {
        SupportTicket ticket = new SupportTicket();
        ticket.setTitle(title);
        ticket.setContent(content);
        ticket.setInquiryType(inquiryType);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setUser(owner);
        ticket.setCreatedAt(createdAt);
        return ticket;
    }
}
