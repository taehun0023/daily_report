package com.example.dailyreport.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataMigrationRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    public DataMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            Integer requestItemExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'request_item'",
                    Integer.class);
            Integer supportTicketExists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'support_ticket'",
                    Integer.class);
            if (requestItemExists == null || supportTicketExists == null || requestItemExists == 0 || supportTicketExists == 0) {
                log.info("마이그레이션 대상 테이블이 없어 스킵합니다.");
                return;
            }

            Integer supportCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM support_ticket", Integer.class);
            if (supportCount != null && supportCount > 0) {
                log.info("support_ticket에 데이터가 있어 마이그레이션을 스킵합니다.");
                return;
            }

            // migrate main tickets
            log.info("support_ticket 마이그레이션을 시작합니다.");
            jdbcTemplate.update("""
                INSERT INTO support_ticket (id, title, content, inquiry_type, category, priority, status, user_id, created_at, processed_at)
                SELECT id, title, content, NULL, NULL, 'MEDIUM', status, user_id, created_at, processed_at
                FROM request_item
            """);

            // migrate file references if column exists
            Integer fileHasOld = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'request_file' AND column_name = 'request_item_id'",
                    Integer.class);
            Integer fileHasNew = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'request_file' AND column_name = 'support_ticket_id'",
                    Integer.class);
            if (fileHasOld != null && fileHasNew != null && fileHasOld > 0 && fileHasNew > 0) {
                jdbcTemplate.update("UPDATE request_file SET support_ticket_id = request_item_id WHERE support_ticket_id IS NULL");
            }

            Integer supportAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM support_ticket", Integer.class);
            Integer requestAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM request_item", Integer.class);
            if (supportAfter != null && requestAfter != null && supportAfter >= requestAfter) {
                jdbcTemplate.execute("DROP TABLE request_item");
                log.info("request_item 테이블을 제거했습니다.");
            }
            log.info("support_ticket 마이그레이션이 완료되었습니다.");
        } catch (Exception e) {
            log.warn("마이그레이션 중 오류가 발생했습니다: {}", e.getMessage());
        }
    }
}
