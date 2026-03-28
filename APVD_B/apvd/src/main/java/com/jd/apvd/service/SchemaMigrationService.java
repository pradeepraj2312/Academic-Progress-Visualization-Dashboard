package com.jd.apvd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaMigrationService {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void normalizeCourseStatusColumns() {
        migrateCourseStatusColumn("courses");
        migrateCourseStatusColumn("student_courses");
    }

    private void migrateCourseStatusColumn(String tableName) {
        try {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN course_status VARCHAR(30) NOT NULL");

            jdbcTemplate.update(
                    "UPDATE " + tableName +
                            " SET course_status = 'ELECTIVE' " +
                            "WHERE course_status IS NULL " +
                            "OR TRIM(course_status) = '' " +
                            "OR UPPER(course_status) NOT IN ('CORE', 'CORE_ELECTIVE', 'OPEN_ELECTIVE', 'ELECTIVE')"
            );

            log.info("Normalized {}.course_status column to VARCHAR(30)", tableName);
        } catch (Exception ex) {
            log.warn("Schema normalization skipped for {}.course_status: {}", tableName, ex.getMessage());
        }
    }
}
