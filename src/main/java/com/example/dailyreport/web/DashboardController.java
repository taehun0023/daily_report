package com.example.dailyreport.web;

import com.example.dailyreport.repository.DailyReportRepository;
import com.example.dailyreport.repository.SupportTicketRepository;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.domain.UserRole;
import com.example.dailyreport.service.CurrentUserProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    private final DailyReportRepository reportRepository;
    private final SupportTicketRepository ticketRepository;
    private final CurrentUserProvider currentUserProvider;

    public DashboardController(DailyReportRepository reportRepository,
                               SupportTicketRepository ticketRepository,
                               CurrentUserProvider currentUserProvider) {
        this.reportRepository = reportRepository;
        this.ticketRepository = ticketRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        List<com.example.dailyreport.domain.DailyReport> reports = reportRepository.findTop7ByOrderByReportDateDesc();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        Map<LocalDate, Long> reportMap = reports.stream()
                .collect(Collectors.toMap(com.example.dailyreport.domain.DailyReport::getReportDate,
                        com.example.dailyreport.domain.DailyReport::getProcessedCount,
                        (a, b) -> a));

        List<com.example.dailyreport.domain.DailyReport> last7 = startDate.datesUntil(today.plusDays(1))
                .map(date -> {
                    com.example.dailyreport.domain.DailyReport report = new com.example.dailyreport.domain.DailyReport();
                    report.setReportDate(date);
                    LocalDateTime from = date.atStartOfDay();
                    LocalDateTime to = date.plusDays(1).atStartOfDay();
                    long count;
                    if (!isAdmin && currentUser != null) {
                        count = ticketRepository.countByProcessedAtBetweenAndUserId(from, to, currentUser.getId());
                    } else {
                        Long reportCount = reportMap.get(date);
                        count = reportCount != null ? reportCount : ticketRepository.countByProcessedAtBetween(from, to);
                    }
                    report.setProcessedCount(count);
                    return report;
                })
                .collect(Collectors.toList());

        long total = 0;
        long max = 0;
        for (var report : last7) {
            long count = report.getProcessedCount();
            total += count;
            if (count > max) {
                max = count;
            }
        }
        double avg = last7.isEmpty() ? 0.0 : (double) total / last7.size();
        int avgTop = max == 0 ? 70 : (int) Math.max(15, Math.min(85, 100 - (avg * 100.0 / max)));
        String avgDisplay = String.format("%.1f", avg);
        boolean hasChart = max > 0;

        model.addAttribute("reports", last7);
        model.addAttribute("totalProcessed", total);
        model.addAttribute("avgProcessed", avgDisplay);
        model.addAttribute("maxProcessed", max);
        model.addAttribute("avgTop", avgTop);
        model.addAttribute("hasChart", hasChart);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        return "dashboard";
    }
}
