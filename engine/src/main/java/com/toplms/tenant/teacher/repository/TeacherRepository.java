package com.toplms.tenant.teacher.repository;

import com.toplms.tenant.teacher.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}