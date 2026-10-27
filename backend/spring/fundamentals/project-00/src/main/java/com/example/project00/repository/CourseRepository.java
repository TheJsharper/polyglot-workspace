package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Course} aggregates.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

	/**
	 * @param courseCode the unique course code
	 * @return the course with that code, if any
	 */
	Optional<Course> findByCourseCode(String courseCode);

	/** @return whether the recruiter demo dataset has been created */
	boolean existsByDemoTrue();

	/** @return the courses of the recruiter demo dataset */
	java.util.List<Course> findByDemoTrueOrderByCourseCode();

}
