package com.toplms.tenant.course.service;

import com.toplms.tenant.course.domain.Course;
import com.toplms.tenant.course.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // CREATE
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    // READ ALL
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // READ ONE
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Course not found with id: " + id));
    }

    // UPDATE
    public Course updateCourse(Long id, Course updatedCourse) {

        Course existingCourse = getCourseById(id);

        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());

        return courseRepository.save(existingCourse);
    }

    // DELETE
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // TOTAL COURSES
    public long getTotalCourses() {
        return courseRepository.count();
    }
}