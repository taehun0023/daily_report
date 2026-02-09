package com.example.dailyreport.scheduler;

import com.example.dailyreport.service.NotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReportScheduler {
    private final JobLauncher jobLauncher;
    private final Job dailyReportJob;
    private final NotificationService notificationService;

    public ReportScheduler(JobLauncher jobLauncher, Job dailyReportJob, NotificationService notificationService) {
        this.jobLauncher = jobLauncher;
        this.dailyReportJob = dailyReportJob;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyReport() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(dailyReportJob, params);
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendMorningNotifications() {
        LocalDate reportDate = LocalDate.now().minusDays(1);
        notificationService.notifyUsersForDate(reportDate);
    }
}
