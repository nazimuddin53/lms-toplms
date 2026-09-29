package com.toplms.tenant.teacher.controller;

import com.toplms.tenant.teacher.domain.Teacher;
import com.toplms.tenant.teacher.service.TeacherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dashboard/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // =========================
    // CREATE
    // =========================

    @GetMapping("/create")
    public String showCreateTeacherForm(Model model) {

        model.addAttribute("teacher", new Teacher());

        return "tenant/teacher/create";
    }

    @PostMapping("/create")
    public String createTeacher(@ModelAttribute Teacher teacher) {

        teacherService.createTeacher(teacher);

        return "redirect:/dashboard/teachers";
    }


    // =========================
    // READ
    // =========================

    @GetMapping
    public String getAllTeachers(Model model) {

        model.addAttribute(
                "teachers",
                teacherService.getAllTeachers()
        );

        return "tenant/teacher/list";
    }


    // =========================
    // EDIT
    // =========================

    @GetMapping("/edit/{id}")
    public String showEditTeacherForm(
            @PathVariable Long id,
            Model model) {

        Teacher teacher = teacherService.getTeacherById(id);

        model.addAttribute("teacher", teacher);

        return "tenant/teacher/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTeacher(
            @PathVariable Long id,
            @ModelAttribute Teacher teacher) {

        teacherService.updateTeacher(id, teacher);

        return "redirect:/dashboard/teachers";
    }


    // =========================
    // DELETE
    // =========================

    @GetMapping("/delete/{id}")
    public String deleteTeacher(@PathVariable Long id) {

        teacherService.deleteTeacher(id);

        return "redirect:/dashboard/teachers";
    }
}