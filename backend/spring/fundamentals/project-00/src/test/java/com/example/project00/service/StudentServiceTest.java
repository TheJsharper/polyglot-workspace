package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.repository.NotesRepository;
import com.example.project00.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

	@Mock
	private StudentRepository studentRepository;

	@Mock
	private NotesRepository notesRepository;

	@InjectMocks
	private StudentService studentService;

	@Test
	void enrollSavesNewStudent() {
		given(studentRepository.findByStudentId("S-1")).willReturn(Optional.empty());
		given(studentRepository.save(any(Student.class))).willAnswer(invocation -> invocation.getArgument(0));

		Student enrolled = studentService.enroll("S-1", "Alan", 2025);

		then(studentRepository).should().save(enrolled);
		assertThat(enrolled.getStudentId()).isEqualTo("S-1");
		assertThat(enrolled.getName()).isEqualTo("Alan");
		assertThat(enrolled.getEnrollmentYear()).isEqualTo(2025);
	}

	@Test
	void enrollRejectsDuplicateStudentId() {
		given(studentRepository.findByStudentId("S-1")).willReturn(Optional.of(new Student("S-1", "Alan", 2025)));

		assertThatThrownBy(() -> studentService.enroll("S-1", "Grace", 2024))
			.isInstanceOf(DuplicateResourceException.class)
			.hasMessage("Student 'S-1' already exists");
		then(studentRepository).should(never()).save(any());
	}

	@Test
	void getByStudentIdThrowsWhenMissing() {
		given(studentRepository.findByStudentId("NOPE")).willReturn(Optional.empty());

		assertThatThrownBy(() -> studentService.getByStudentId("NOPE"))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Student 'NOPE' not found");
	}

	@Test
	void findAllDelegatesToRepository() {
		List<Student> students = List.of(new Student("S-1", "Alan", 2025));
		given(studentRepository.findAll()).willReturn(students);

		assertThat(studentService.findAll()).isEqualTo(students);
	}

	@Test
	void updateChangesNameAndEnrollmentYear() {
		Student student = new Student("S-1", "Alan", 2025);
		given(studentRepository.findByStudentId("S-1")).willReturn(Optional.of(student));

		studentService.update("S-1", "Alan Turing", 2024);

		assertThat(student.getName()).isEqualTo("Alan Turing");
		assertThat(student.getEnrollmentYear()).isEqualTo(2024);
	}

	@Test
	void deleteUnlinksAttendanceAndRemovesStudent() {
		Student student = new Student("S-1", "Alan", 2025);
		Lecture lecture = lecture();
		lecture.addAttendee(student);
		given(studentRepository.findByStudentId("S-1")).willReturn(Optional.of(student));
		given(notesRepository.findByStudent(student)).willReturn(List.of());

		studentService.delete("S-1");

		assertThat(lecture.getAttendees()).isEmpty();
		assertThat(student.getAttendedLectures()).isEmpty();
		then(studentRepository).should().delete(student);
	}

	@Test
	void deleteRejectsStudentWithNotes() {
		Student student = new Student("S-1", "Alan", 2025);
		Lecture lecture = lecture();
		lecture.addAttendee(student);
		given(studentRepository.findByStudentId("S-1")).willReturn(Optional.of(student));
		given(notesRepository.findByStudent(student)).willReturn(List.of(new Notes(student, lecture, "Notes", false)));

		assertThatThrownBy(() -> studentService.delete("S-1")).isInstanceOf(IllegalStateException.class)
			.hasMessage("Student S-1 still has notes");
		assertThat(lecture.hasAttendee(student)).isTrue();
		then(studentRepository).should(never()).delete(any());
	}

	private static Lecture lecture() {
		return new Lecture(new Course("CS101", "Programming", 6), new Professor("E-1", "Ada", "CS"),
				LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
	}

}
