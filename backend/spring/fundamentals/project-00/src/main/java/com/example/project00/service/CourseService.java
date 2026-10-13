package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Course;
import com.example.project00.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

	private final CourseRepository courseRepository;

	public Course create(String courseCode, String title, int credits) {
		if (courseRepository.findByCourseCode(courseCode).isPresent()) {
			throw new DuplicateResourceException("Course", courseCode);
		}
		return courseRepository.save(new Course(courseCode, title, credits));
	}

	@Transactional(readOnly = true)
	public Course getByCode(String courseCode) {
		return courseRepository.findByCourseCode(courseCode)
			.orElseThrow(() -> new ResourceNotFoundException("Course", courseCode));
	}

	@Transactional(readOnly = true)
	public List<Course> findAll() {
		return courseRepository.findAll();
	}

	public Course update(String courseCode, String title, int credits) {
		Course course = getByCode(courseCode);
		course.setTitle(title);
		course.setCredits(credits);
		return course;
	}

	public void delete(String courseCode) {
		courseRepository.delete(getByCode(courseCode));
	}

}
