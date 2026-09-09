package com.toplms.tenant.course.controller;

import com.toplms.tenant.course.domain.Course;
import com.toplms.tenant.course.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dashboard/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // =========================
    // READ - Course List
    // =========================
    @GetMapping
    public String courses(Model model) {

        model.addAttribute(
                "courses",
                courseService.getAllCourses()
        );

        return "tenant/course/courses";
    }

    // =========================
    // CREATE - Show Form
    // =========================
    @GetMapping("/create")
    public String createCourseForm(Model model) {

        model.addAttribute("course", new Course());

        return "tenant/course/create";
    }

    // =========================
    // CREATE - Save Course
    // =========================
    @PostMapping
    public String createCourse(
            @ModelAttribute Course course) {

        courseService.createCourse(course);

        return "redirect:/dashboard/courses";
    }

    // =========================
    // UPDATE - Show Form
    // =========================
    @GetMapping("/edit/{id}")
    public String editCourseForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "course",
                courseService.getCourseById(id)
        );

        return "tenant/course/edit";
    }

    // =========================
    // UPDATE - Save Changes
    // =========================
    @PostMapping("/edit/{id}")
    public String updateCourse(
            @PathVariable Long id,
            @ModelAttribute Course course) {

        courseService.updateCourse(id, course);

        return "redirect:/dashboard/courses";
    }

    // =========================
    // DELETE
    // =========================
    @PostMapping("/delete/{id}")
    public String deleteCourse(
            @PathVariable Long id) {

        courseService.deleteCourse(id);

        return "redirect:/dashboard/courses";
    }
}