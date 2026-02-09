package com.example.dailyreport.repository;

import com.example.dailyreport.domain.SupportTicket;
import com.example.dailyreport.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    @Override
    @EntityGraph(attributePaths = {"user", "files"})
    List<SupportTicket> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "files"})
    Optional<SupportTicket> findById(Long id);

    long countByProcessedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByProcessedAtBetweenAndUserId(LocalDateTime start, LocalDateTime end, Long userId);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<SupportTicket> findByProcessedAtBetween(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"user", "files"})
    @Query("""
            select r from SupportTicket r
            where (:status is null or r.status = :status)
              and (:userId is null or r.user.id = :userId)
              and (:inquiryType is null or r.inquiryType = :inquiryType)
              and (:category is null or r.category = :category)
              and (:fromDate is null or r.createdAt >= :fromDate)
              and (:toDate is null or r.createdAt < :toDate)
            order by r.createdAt desc
            """)
    List<SupportTicket> findFiltered(@Param("status") TicketStatus status,
                                   @Param("userId") Long userId,
                                   @Param("inquiryType") String inquiryType,
                                   @Param("category") String category,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);
}
