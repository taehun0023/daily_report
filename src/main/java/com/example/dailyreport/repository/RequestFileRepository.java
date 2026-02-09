package com.example.dailyreport.repository;

import com.example.dailyreport.domain.RequestFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestFileRepository extends JpaRepository<RequestFile, Long> {
}
