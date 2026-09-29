package com.toplms.tenant.student.repository;

import com.toplms.tenant.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}