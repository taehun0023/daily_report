package com.example.dailyreport.service;

import com.example.dailyreport.domain.RequestFile;
import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketStatus;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketService {
    private final SupportTicketRepository ticketRepository;
    private final FileStorageService fileStorageService;
    private final UserAccountRepository userRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                          FileStorageService fileStorageService,
                          UserAccountRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    public List<SupportTicket> findAll() {
        return ticketRepository.findAll();
    }

    public List<SupportTicket> findFiltered(TicketStatus status, Long userId, String inquiryType, String category, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        return ticketRepository.findFiltered(status, userId, inquiryType, category, fromDateTime, toDateTime);
    }

    public SupportTicket findById(Long id) {
        return ticketRepository.findById(id).orElseThrow();
    }

    @Transactional
    public SupportTicket create(SupportTicket item, List<MultipartFile> files) throws IOException {
        if (item.getUser() == null || item.getUser().getId() == null) {
            throw new IllegalArgumentException("사용자를 선택해야 합니다.");
        }
        item.setUser(userRepository.getReferenceById(item.getUser().getId()));
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    RequestFile requestFile = fileStorageService.store(file);
                    item.addFile(requestFile);
                }
            }
        }
        return ticketRepository.save(item);
    }

    @Transactional
    public SupportTicket update(Long id, SupportTicket updates) {
        SupportTicket item = findById(id);
        item.setTitle(updates.getTitle());
        item.setContent(updates.getContent());
        if (updates.getUser() != null && updates.getUser().getId() != null) {
            item.setUser(userRepository.getReferenceById(updates.getUser().getId()));
        }
        return item;
    }

    @Transactional
    public void delete(Long id) {
        ticketRepository.deleteById(id);
    }

    @Transactional
    public SupportTicket updateStatus(Long id, TicketStatus status) {
        SupportTicket item = findById(id);
        item.setStatus(status);
        if (status == TicketStatus.DONE) {
            item.setProcessedAt(LocalDateTime.now());
        } else {
            item.setProcessedAt(null);
        }
        return item;
    }
}
