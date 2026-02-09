package com.example.dailyreport.service;

import com.example.dailyreport.domain.DailyReport;
import com.example.dailyreport.repository.DailyReportRepository;
import com.example.dailyreport.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReportService {
    private final DailyReportRepository reportRepository;
    private final SupportTicketRepository ticketRepository;

    public ReportService(DailyReportRepository reportRepository, SupportTicketRepository ticketRepository) {
        this.reportRepository = reportRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public DailyReport createOrUpdateReport(LocalDate reportDate) {
        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();
        long count = ticketRepository.countByProcessedAtBetween(start, end);

        DailyReport report = reportRepository.findByReportDate(reportDate).orElseGet(DailyReport::new);
        report.setReportDate(reportDate);
        report.setProcessedCount(count);
        return reportRepository.save(report);
    }
}
