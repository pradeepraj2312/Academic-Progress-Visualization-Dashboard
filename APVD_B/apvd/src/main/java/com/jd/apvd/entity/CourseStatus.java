package com.jd.apvd.entity;

/**
 * Enum for Course Status
 * CORE: Core courses (students can select up to 4)
 * CORE_ELECTIVE: Department elective courses (students can select up to 2)
 * OPEN_ELECTIVE: Open elective courses from other departments (students can select up to 1)
 * ELECTIVE: Backward-compatible alias treated as CORE_ELECTIVE in selection rules
 */
public enum CourseStatus {
    CORE,
    CORE_ELECTIVE,
    OPEN_ELECTIVE,
    ELECTIVE
}
