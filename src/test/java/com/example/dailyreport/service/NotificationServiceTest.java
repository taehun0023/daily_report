package com.example.dailyreport.service;

import com.example.dailyreport.domain.Notification;
import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.repository.NotificationRepository;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private UserAccountRepository userRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyUsersForDateAggregatesByAssignee() {
        UserAccount user1 = new UserAccount();
        ReflectionTestUtils.setField(user1, "id", 1L);
        user1.setEmail("u1@example.com");
        user1.setName("u1");

        UserAccount user2 = new UserAccount();
        ReflectionTestUtils.setField(user2, "id", 2L);
        user2.setEmail("u2@example.com");
        user2.setName("u2");

        SupportTicket t1 = new SupportTicket();
        t1.setTitle("t1");
        t1.setContent("c");
        t1.setUser(user1);
        SupportTicket t2 = new SupportTicket();
        t2.setTitle("t2");
        t2.setContent("c");
        t2.setUser(user1);
        SupportTicket t3 = new SupportTicket();
        t3.setTitle("t3");
        t3.setContent("c");
        t3.setUser(user2);

        given(ticketRepository.findByProcessedAtBetween(any(), any())).willReturn(List.of(t1, t2, t3));
        given(userRepository.findById(1L)).willReturn(Optional.of(user1));
        given(userRepository.findById(2L)).willReturn(Optional.of(user2));
        given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> invocation.getArgument(0));

        int created = notificationService.notifyUsersForDate(LocalDate.of(2026, 2, 10));

        assertEquals(2, created);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(mailService, times(2)).send(any(), any(), any());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        Set<String> messages = captor.getAllValues().stream().map(Notification::getMessage).collect(Collectors.toSet());
        assertEquals(Set.of("전날 처리 완료 티켓 수: 2건", "전날 처리 완료 티켓 수: 1건"), messages);
    }
}
