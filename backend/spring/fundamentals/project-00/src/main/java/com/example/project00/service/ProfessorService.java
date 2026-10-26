package com.example.project00.service;

import java.util.Comparator;
import java.util.List;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.ProfessorRepository;
import com.example.project00.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for managing teaching staff. All methods run in a transaction.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProfessorService {

	private final ProfessorRepository professorRepository;

	private final UserAccountRepository userAccountRepository;

	private final CourseRepository courseRepository;

	/**
	 * Hires a professor.
	 * @return the persisted professor
	 * @throws DuplicateResourceException if the employee id is already taken
	 */
	public Professor hire(String employeeId, String name, String department) {
		if (professorRepository.findByEmployeeId(employeeId).isPresent()) {
			throw new DuplicateResourceException("Professor", employeeId);
		}
		return professorRepository.save(new Professor(employeeId, name, department));
	}

	/**
	 * @param employeeId the unique employee id
	 * @return the professor
	 * @throws ResourceNotFoundException if no professor has that employee id
	 */
	@Transactional(readOnly = true)
	public Professor getByEmployeeId(String employeeId) {
		return professorRepository.findByEmployeeId(employeeId)
			.orElseThrow(() -> new ResourceNotFoundException("Professor", employeeId));
	}

	/**
	 * @param employeeId the professor's employee id
	 * @return the distinct students enrolled in the courses the professor teaches
	 * @throws ResourceNotFoundException if no professor has that employee id
	 */
	@Transactional(readOnly = true)
	public List<Student> findStudents(String employeeId) {
		Professor professor = getByEmployeeId(employeeId);
		return courseRepository.findAll()
			.stream()
			.filter(course -> course.isTaughtBy(professor))
			.flatMap(course -> course.getEnrolledStudents().stream())
			.distinct()
			.sorted(Comparator.comparing(Student::getStudentId))
			.toList();
	}

	/** @return all professors */
	@Transactional(readOnly = true)
	public List<Professor> findAll() {
		return professorRepository.findAll();
	}

	/**
	 * Changes a professor's name and department.
	 * @return the updated professor
	 * @throws ResourceNotFoundException if no professor has that employee id
	 */
	public Professor update(String employeeId, String name, String department) {
		Professor professor = getByEmployeeId(employeeId);
		professor.setName(name);
		professor.setDepartment(department);
		return professor;
	}

	/**
	 * Removes a professor together with their teaching assignments and login account.
	 * @throws ResourceNotFoundException if no professor has that employee id
	 * @throws IllegalStateException if the professor still delivers lectures
	 */
	public void delete(String employeeId) {
		Professor professor = getByEmployeeId(employeeId);
		if (!professor.getLectures().isEmpty()) {
			throw new IllegalStateException(
					"Professor " + employeeId + " still delivers " + professor.getLectures().size() + " lecture(s)");
		}
		List.copyOf(professor.getTaughtCourses()).forEach(course -> course.unassignTeacher(professor));
		userAccountRepository.deleteByProfessor(professor);
		professorRepository.delete(professor);
	}

}
