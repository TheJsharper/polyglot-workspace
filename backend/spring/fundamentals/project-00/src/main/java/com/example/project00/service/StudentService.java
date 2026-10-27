package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Student;
import com.example.project00.repository.NotesRepository;
import com.example.project00.repository.StudentRepository;
import com.example.project00.repository.UserAccountRepository;
import com.example.project00.security.DemoScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for managing enrolled students. All methods run in a transaction.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;

	private final NotesRepository notesRepository;

	private final UserAccountRepository userAccountRepository;

	/**
	 * Enrolls a student.
	 * @return the persisted student
	 * @throws DuplicateResourceException if the student id is already taken
	 */
	public Student enroll(String studentId, String name, int enrollmentYear) {
		DemoIds.requireNotReserved(studentId);
		if (studentRepository.findByStudentId(studentId).isPresent()) {
			throw new DuplicateResourceException("Student", studentId);
		}
		return studentRepository.save(new Student(studentId, name, enrollmentYear));
	}

	/**
	 * @param studentId the unique student id
	 * @return the student
	 * @throws ResourceNotFoundException if no student has that student id
	 */
	@Transactional(readOnly = true)
	public Student getByStudentId(String studentId) {
		return studentRepository.findByStudentId(studentId)
			.filter(student -> DemoScope.visible(student.isDemo()))
			.orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
	}

	/** @return all students */
	@Transactional(readOnly = true)
	public List<Student> findAll() {
		return studentRepository.findAll().stream().filter(student -> DemoScope.visible(student.isDemo())).toList();
	}

	/**
	 * Changes a student's name and enrollment year.
	 * @return the updated student
	 * @throws ResourceNotFoundException if no student has that student id
	 */
	public Student update(String studentId, String name, int enrollmentYear) {
		Student student = getByStudentId(studentId);
		student.setName(name);
		student.setEnrollmentYear(enrollmentYear);
		return student;
	}

	/**
	 * Removes a student together with their attendance, enrollments and login account.
	 * @throws ResourceNotFoundException if no student has that student id
	 * @throws IllegalStateException if the student has written notes
	 */
	public void delete(String studentId) {
		Student student = getByStudentId(studentId);
		if (!notesRepository.findByStudent(student).isEmpty()) {
			throw new IllegalStateException("Student " + studentId + " still has notes");
		}
		// Lecture owns the attendance join table, so unlink from that side before deleting.
		List.copyOf(student.getAttendedLectures()).forEach(lecture -> lecture.removeAttendee(student));
		List.copyOf(student.getEnrolledCourses()).forEach(course -> course.unenroll(student));
		userAccountRepository.deleteByStudent(student);
		studentRepository.delete(student);
	}

}
