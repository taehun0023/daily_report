package com.example.dailyreport.batch;

import com.example.dailyreport.service.ReportService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
public class DailyReportJobConfig {

    @Bean
    public Job dailyReportJob(JobRepository jobRepository, Step dailyReportStep) {
        return new JobBuilder("dailyReportJob", jobRepository)
                .start(dailyReportStep)
                .build();
    }

    @Bean
    public Step dailyReportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                ReportService reportService) {
        return new StepBuilder("dailyReportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDate reportDate = LocalDate.now().minusDays(1);
                    reportService.createOrUpdateReport(reportDate);
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
