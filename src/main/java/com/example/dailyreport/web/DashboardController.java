package com.example.dailyreport.web;

import com.example.dailyreport.repository.DailyReportRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {
    private final DailyReportRepository reportRepository;

    public DashboardController(DailyReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<com.example.dailyreport.domain.DailyReport> reports = reportRepository.findTop7ByOrderByReportDateDesc();
        long total = 0;
        long max = 0;
        for (var report : reports) {
            long count = report.getProcessedCount();
            total += count;
            if (count > max) {
                max = count;
            }
        }
        long avg = reports.isEmpty() ? 0 : total / reports.size();
        int avgTop = max == 0 ? 70 : (int) Math.max(15, Math.min(85, 100 - (avg * 100.0 / max)));
        boolean hasChart = max > 0;

        model.addAttribute("reports", reports);
        model.addAttribute("totalProcessed", total);
        model.addAttribute("avgProcessed", avg);
        model.addAttribute("maxProcessed", max);
        model.addAttribute("avgTop", avgTop);
        model.addAttribute("hasChart", hasChart);
        return "dashboard";
    }
}
