package com.example.dailyreport.web;

// 테스트에 사용할 도메인/의존성 import
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

// BDD 스타일 Mock 설정용
import static org.mockito.BDDMockito.given;

// MockMvc 요청/검증 메서드 import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * DashboardController만 로딩하여
 * 웹(MVC) 계층만 테스트하는 설정
 */
@WebMvcTest(DashboardController.class)

/**
 * Spring Security 필터 비활성화
 * (로그인/권한 없이 테스트 가능하도록)
 */
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

        /**
         * 가짜 HTTP 요청을 보내기 위한 객체
         * Controller 테스트의 핵심 도구
         */
        @Autowired
        private MockMvc mockMvc;

        /**
         * 실제 Repository 대신 Mock 객체를 주입
         * DB 접근 없이 Controller만 테스트하기 위함
         */
        @MockBean
        private DailyReportRepository reportRepository;

        /**
         * SupportTicketRepository도 Mock 처리
         */
        @MockBean
        private SupportTicketRepository ticketRepository;

        /**
         * 현재 로그인 사용자 제공 서비스 Mock 처리
         */
        @MockBean
        private CurrentUserProvider currentUserProvider;

        /**
         * /dashboard 요청 시
         * 정상적으로 View와 Model이 반환되는지 테스트
         */
        @Test
        void dashboardRendersWithMetrics() throws Exception {

                // =========================
                // given (사전 준비 단계)
                // =========================

                // 가짜 DailyReport 객체 생성
                DailyReport report = new DailyReport();
                report.setReportDate(LocalDate.of(2026, 2, 7));
                report.setProcessedCount(3);

                // Controller 내부에서 이 메서드가 호출되면
                // 위에서 만든 report를 반환하도록 설정
                given(reportRepository.findTop7ByOrderByReportDateDesc())
                                .willReturn(List.of(report));

                // 현재 로그인 사용자는 없다고 가정
                given(currentUserProvider.getCurrentUser())
                                .willReturn(null);

                // =========================
                // when + then
                // =========================

                mockMvc.perform(get("/dashboard")) // GET /dashboard 요청 수행

                                // HTTP 상태 코드 200 OK 확인
                                .andExpect(status().isOk())

                                // 반환되는 View 이름이 "dashboard"인지 확인
                                .andExpect(view().name("dashboard"))

                                // Model에 "reports"라는 속성이 존재하는지 확인
                                .andExpect(model().attributeExists("reports"))

                                // Model에 총 처리 건수 데이터가 존재하는지 확인
                                .andExpect(model().attributeExists("totalProcessed"))

                                // Model에 평균 처리 건수 데이터가 존재하는지 확인
                                .andExpect(model().attributeExists("avgProcessed"))

                                // Model에 최대 처리 건수 데이터가 존재하는지 확인
                                .andExpect(model().attributeExists("maxProcessed"));
        }
}
