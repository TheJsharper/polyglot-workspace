package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

	Optional<Course> findByCourseCode(String courseCode);

}
