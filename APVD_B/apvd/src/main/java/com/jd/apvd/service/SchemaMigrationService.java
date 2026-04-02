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
        migrateStudentMarksSubjectNameColumns();
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

    private void migrateStudentMarksSubjectNameColumns() {
        for (int i = 1; i <= 6; i++) {
            String columnName = "subject" + i + "_name";
            String fallbackValue = "Subject " + i;
            try {
                jdbcTemplate.execute("ALTER TABLE student_marks ADD COLUMN " + columnName + " VARCHAR(255) NOT NULL DEFAULT '" + fallbackValue + "'");
                log.info("Added student_marks.{} column", columnName);
            } catch (Exception ex) {
                log.debug("student_marks.{} add skipped: {}", columnName, ex.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE student_marks MODIFY COLUMN " + columnName + " VARCHAR(255) NOT NULL");
                jdbcTemplate.update(
                        "UPDATE student_marks SET " + columnName + " = ? WHERE " + columnName + " IS NULL OR TRIM(" + columnName + ") = ''",
                        fallbackValue
                );
            } catch (Exception ex) {
                log.warn("Schema normalization skipped for student_marks.{}: {}", columnName, ex.getMessage());
            }
        }
    }
}
