package com.example.dailyreport.web;

import com.example.dailyreport.domain.DailyReport;
import com.example.dailyreport.repository.DailyReportRepository;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.service.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DailyReportRepository reportRepository;

    @MockBean
    private SupportTicketRepository ticketRepository;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void dashboardRendersWithMetrics() throws Exception {
        DailyReport report = new DailyReport();
        report.setReportDate(LocalDate.of(2026, 2, 7));
        report.setProcessedCount(3);
        given(reportRepository.findTop7ByOrderByReportDateDesc()).willReturn(List.of(report));
        given(currentUserProvider.getCurrentUser()).willReturn(null);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("reports"))
                .andExpect(model().attributeExists("totalProcessed"))
                .andExpect(model().attributeExists("avgProcessed"))
                .andExpect(model().attributeExists("maxProcessed"));
    }
}
