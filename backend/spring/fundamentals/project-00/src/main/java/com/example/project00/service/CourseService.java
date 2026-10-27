package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Course;
import com.example.project00.repository.CourseRepository;
import com.example.project00.security.DemoScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for managing the course catalogue. All methods run in a transaction.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

	private final CourseRepository courseRepository;

	private final StudentService studentService;

	private final ProfessorService professorService;

	/**
	 * Creates a course.
	 * @return the persisted course
	 * @throws DuplicateResourceException if the course code is already taken
	 */
	public Course create(String courseCode, String title, int credits) {
		DemoIds.requireNotReserved(courseCode);
		if (courseRepository.findByCourseCode(courseCode).isPresent()) {
			throw new DuplicateResourceException("Course", courseCode);
		}
		return courseRepository.save(new Course(courseCode, title, credits));
	}

	/**
	 * @param courseCode the unique course code
	 * @return the course
	 * @throws ResourceNotFoundException if no course has that code
	 */
	@Transactional(readOnly = true)
	public Course getByCode(String courseCode) {
		return courseRepository.findByCourseCode(courseCode)
			.filter(course -> DemoScope.visible(course.isDemo()))
			.orElseThrow(() -> new ResourceNotFoundException("Course", courseCode));
	}

	/** @return all courses */
	@Transactional(readOnly = true)
	public List<Course> findAll() {
		return courseRepository.findAll().stream().filter(course -> DemoScope.visible(course.isDemo())).toList();
	}

	/**
	 * Changes a course's title and credits.
	 * @return the updated course
	 * @throws ResourceNotFoundException if no course has that code
	 */
	public Course update(String courseCode, String title, int credits) {
		Course course = getByCode(courseCode);
		course.setTitle(title);
		course.setCredits(credits);
		return course;
	}

	/**
	 * Deletes a course together with its lectures.
	 * @throws ResourceNotFoundException if no course has that code
	 */
	public void delete(String courseCode) {
		courseRepository.delete(getByCode(courseCode));
	}

	/**
	 * Enrolls a student in a course (idempotent).
	 * @return the updated course
	 * @throws ResourceNotFoundException if the course or student does not exist
	 */
	public Course enroll(String courseCode, String studentId) {
		Course course = getByCode(courseCode);
		course.enroll(studentService.getByStudentId(studentId));
		return course;
	}

	/**
	 * Removes a student's enrollment from a course.
	 * @return the updated course
	 * @throws ResourceNotFoundException if the course or student does not exist
	 * @throws IllegalStateException if the student still attends lectures of the course
	 */
	public Course unenroll(String courseCode, String studentId) {
		Course course = getByCode(courseCode);
		course.unenroll(studentService.getByStudentId(studentId));
		return course;
	}

	/**
	 * Lets a professor teach a course (idempotent).
	 * @return the updated course
	 * @throws ResourceNotFoundException if the course or professor does not exist
	 */
	public Course assignTeacher(String courseCode, String employeeId) {
		Course course = getByCode(courseCode);
		course.assignTeacher(professorService.getByEmployeeId(employeeId));
		return course;
	}

	/**
	 * Stops a professor from teaching a course.
	 * @return the updated course
	 * @throws ResourceNotFoundException if the course or professor does not exist
	 * @throws IllegalStateException if the professor still delivers lectures of the course
	 */
	public Course unassignTeacher(String courseCode, String employeeId) {
		Course course = getByCode(courseCode);
		course.unassignTeacher(professorService.getByEmployeeId(employeeId));
		return course;
	}

}
