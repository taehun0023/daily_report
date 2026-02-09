package com.example.dailyreport;

import com.example.dailyreport.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FileStorageProperties.class)
public class DailyReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(DailyReportApplication.class, args);
    }
}
