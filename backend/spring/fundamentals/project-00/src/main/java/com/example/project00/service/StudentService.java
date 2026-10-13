package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Student;
import com.example.project00.repository.NotesRepository;
import com.example.project00.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;

	private final NotesRepository notesRepository;

	public Student enroll(String studentId, String name, int enrollmentYear) {
		if (studentRepository.findByStudentId(studentId).isPresent()) {
			throw new DuplicateResourceException("Student", studentId);
		}
		return studentRepository.save(new Student(studentId, name, enrollmentYear));
	}

	@Transactional(readOnly = true)
	public Student getByStudentId(String studentId) {
		return studentRepository.findByStudentId(studentId)
			.orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
	}

	@Transactional(readOnly = true)
	public List<Student> findAll() {
		return studentRepository.findAll();
	}

	public Student update(String studentId, String name, int enrollmentYear) {
		Student student = getByStudentId(studentId);
		student.setName(name);
		student.setEnrollmentYear(enrollmentYear);
		return student;
	}

	public void delete(String studentId) {
		Student student = getByStudentId(studentId);
		if (!notesRepository.findByStudent(student).isEmpty()) {
			throw new IllegalStateException("Student " + studentId + " still has notes");
		}
		// Lecture owns the attendance join table, so unlink from that side before deleting.
		List.copyOf(student.getAttendedLectures()).forEach(lecture -> lecture.removeAttendee(student));
		studentRepository.delete(student);
	}

}
