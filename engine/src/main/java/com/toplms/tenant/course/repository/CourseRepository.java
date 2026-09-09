package com.toplms.tenant.course.repository;

import com.toplms.tenant.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}