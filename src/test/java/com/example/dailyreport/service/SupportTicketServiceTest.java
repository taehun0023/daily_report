package com.example.dailyreport.service;

import com.example.dailyreport.domain.RequestFile;
import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketStatus;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserAccountRepository userRepository;

    @InjectMocks
    private SupportTicketService supportTicketService;

    @Test
    void createThrowsWhenUserMissing() {
        SupportTicket ticket = new SupportTicket();
        ticket.setTitle("t");
        ticket.setContent("c");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> supportTicketService.create(ticket, null));
        assertEquals("사용자를 선택해야 합니다.", ex.getMessage());
    }

    @Test
    void createStoresOnlyNonEmptyFilesAndSaves() throws Exception {
        UserAccount user = new UserAccount();
        ReflectionTestUtils.setField(user, "id", 1L);

        SupportTicket ticket = new SupportTicket();
        ticket.setTitle("t");
        ticket.setContent("c");
        ticket.setUser(user);

        MultipartFile emptyFile = org.mockito.Mockito.mock(MultipartFile.class);
        MultipartFile realFile = org.mockito.Mockito.mock(MultipartFile.class);
        RequestFile stored = new RequestFile();
        stored.setOriginalName("a.txt");
        stored.setStoredName("stored-a.txt");
        stored.setMimeType("text/plain");
        stored.setSize(10L);

        given(emptyFile.isEmpty()).willReturn(true);
        given(realFile.isEmpty()).willReturn(false);
        given(userRepository.getReferenceById(1L)).willReturn(user);
        given(fileStorageService.store(realFile)).willReturn(stored);
        given(ticketRepository.save(any(SupportTicket.class))).willAnswer(invocation -> invocation.getArgument(0));

        SupportTicket saved = supportTicketService.create(ticket, List.of(emptyFile, realFile));

        assertEquals(user, saved.getUser());
        assertEquals(1, saved.getFiles().size());
        assertEquals("stored-a.txt", saved.getFiles().get(0).getStoredName());
        verify(fileStorageService, times(1)).store(realFile);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void updateStatusDoneSetsProcessedAt() {
        SupportTicket ticket = new SupportTicket();
        ticket.setTitle("t");
        ticket.setContent("c");

        given(ticketRepository.findById(10L)).willReturn(Optional.of(ticket));

        SupportTicket updated = supportTicketService.updateStatus(10L, TicketStatus.DONE);

        assertEquals(TicketStatus.DONE, updated.getStatus());
        assertNotNull(updated.getProcessedAt());
        assertTrue(updated.getProcessedAt().toString().length() > 0);
    }
}
