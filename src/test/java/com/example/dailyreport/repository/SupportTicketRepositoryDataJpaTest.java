package com.example.dailyreport.repository;

import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketPriority;
import com.example.dailyreport.domain.TicketStatus;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:datajpatest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SupportTicketRepositoryDataJpaTest {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void findFilteredReturnsOnlyMatchingAssigneeAndDateRange() {
        UserAccount assigneeA = new UserAccount();
        assigneeA.setName("A");
        assigneeA.setEmail("a@test.com");
        assigneeA.setPassword("pw");
        assigneeA.setRole(UserRole.MANAGER);
        userAccountRepository.save(assigneeA);

        UserAccount assigneeB = new UserAccount();
        assigneeB.setName("B");
        assigneeB.setEmail("b@test.com");
        assigneeB.setPassword("pw");
        assigneeB.setRole(UserRole.MANAGER);
        userAccountRepository.save(assigneeB);

        SupportTicket match = new SupportTicket();
        match.setTitle("match");
        match.setContent("content");
        match.setInquiryType("로그인");
        match.setCategory("계정");
        match.setPriority(TicketPriority.MEDIUM);
        match.setStatus(TicketStatus.RECEIVED);
        match.setUser(assigneeA);
        match.setCreatedAt(LocalDateTime.of(2026, 2, 10, 10, 0));
        supportTicketRepository.save(match);

        SupportTicket otherUser = new SupportTicket();
        otherUser.setTitle("otherUser");
        otherUser.setContent("content");
        otherUser.setInquiryType("로그인");
        otherUser.setCategory("계정");
        otherUser.setPriority(TicketPriority.MEDIUM);
        otherUser.setStatus(TicketStatus.RECEIVED);
        otherUser.setUser(assigneeB);
        otherUser.setCreatedAt(LocalDateTime.of(2026, 2, 10, 11, 0));
        supportTicketRepository.save(otherUser);

        SupportTicket outOfRange = new SupportTicket();
        outOfRange.setTitle("out");
        outOfRange.setContent("content");
        outOfRange.setInquiryType("로그인");
        outOfRange.setCategory("계정");
        outOfRange.setPriority(TicketPriority.MEDIUM);
        outOfRange.setStatus(TicketStatus.RECEIVED);
        outOfRange.setUser(assigneeA);
        outOfRange.setCreatedAt(LocalDateTime.of(2026, 2, 9, 23, 0));
        supportTicketRepository.save(outOfRange);

        LocalDate from = LocalDate.of(2026, 2, 10);
        LocalDate to = LocalDate.of(2026, 2, 10);
        List<SupportTicket> result = supportTicketRepository.findFiltered(
                TicketStatus.RECEIVED,
                assigneeA.getId(),
                "로그인",
                "계정",
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay()
        );

        assertEquals(1, result.size());
        assertEquals("match", result.get(0).getTitle());
    }
}
