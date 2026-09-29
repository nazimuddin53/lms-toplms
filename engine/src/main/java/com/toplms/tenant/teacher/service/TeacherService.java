package com.toplms.tenant.teacher.service;

import com.toplms.tenant.teacher.domain.Teacher;
import com.toplms.tenant.teacher.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    // CREATE
    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    // READ ALL
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // READ ONE
    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Teacher not found with id: " + id));
    }

    // UPDATE
    public Teacher updateTeacher(Long id, Teacher updatedTeacher) {

        Teacher existingTeacher = getTeacherById(id);

        existingTeacher.setName(updatedTeacher.getName());
        existingTeacher.setEmail(updatedTeacher.getEmail());

        return teacherRepository.save(existingTeacher);
    }

    // DELETE
    public void deleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }

    // TOTAL TEACHERS
    public long getTotalTeachers() {
        return teacherRepository.count();
    }
}