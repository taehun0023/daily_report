package com.example.dailyreport.service;

import com.example.dailyreport.domain.DailyReport;
import com.example.dailyreport.repository.DailyReportRepository;
import com.example.dailyreport.repository.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DailyReportRepository reportRepository;

    @Mock
    private SupportTicketRepository ticketRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void createOrUpdateReportCreatesNewReportWhenMissing() {
        LocalDate reportDate = LocalDate.of(2026, 2, 10);
        given(ticketRepository.countByProcessedAtBetween(any(), any())).willReturn(5L);
        given(reportRepository.findByReportDate(reportDate)).willReturn(Optional.empty());
        given(reportRepository.save(any(DailyReport.class))).willAnswer(invocation -> invocation.getArgument(0));

        DailyReport result = reportService.createOrUpdateReport(reportDate);

        assertEquals(reportDate, result.getReportDate());
        assertEquals(5L, result.getProcessedCount());
        verify(reportRepository).save(any(DailyReport.class));
    }
}
