package com.toplms.tenant.student.controller;

import com.toplms.tenant.student.domain.Student;
import com.toplms.tenant.student.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dashboard/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // =========================
    // CREATE
    // =========================

    @GetMapping("/create")
    public String showCreateStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "tenant/student/create";
    }

    @PostMapping("/create")
    public String createStudent(@ModelAttribute Student student) {
        studentService.createStudent(student);
        return "redirect:/dashboard/students";
    }


    // =========================
    // READ
    // =========================

    @GetMapping
    public String getAllStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "tenant/student/list";
    }


    // =========================
    // EDIT
    // =========================

    @GetMapping("/edit/{id}")
    public String showEditStudentForm(
            @PathVariable Long id,
            Model model) {

        Student student = studentService.getStudentById(id);

        model.addAttribute("student", student);

        return "tenant/student/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateStudent(
            @PathVariable Long id,
            @ModelAttribute Student student) {

        studentService.updateStudent(id, student);

        return "redirect:/dashboard/students";
    }


    // =========================
    // DELETE
    // =========================

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {

        studentService.deleteStudent(id);

        return "redirect:/dashboard/students";
    }
}