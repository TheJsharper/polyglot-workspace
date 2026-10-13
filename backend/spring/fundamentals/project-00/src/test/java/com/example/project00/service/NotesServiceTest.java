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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

	@Mock
	private NotesRepository notesRepository;

	@Mock
	private StudentService studentService;

	@Mock
	private LectureService lectureService;

	@Mock
	private ProfessorService professorService;

	@InjectMocks
	private NotesService notesService;

	private Professor professor;

	private Student student;

	private Lecture lecture;

	@BeforeEach
	void setUp() {
		professor = new Professor("E-1", "Ada", "CS");
		student = new Student("S-1", "Alan", 2025);
		lecture = new Lecture(new Course("CS101", "Programming", 6), professor, LocalDate.of(2026, 10, 14),
				LocalTime.of(9, 0), "Variables", "A-101");
		lecture.addAttendee(student);
	}

	@Test
	void writeSavesNotesForAttendingStudent() {
		given(studentService.getByStudentId("S-1")).willReturn(student);
		given(lectureService.getById(1L)).willReturn(lecture);
		given(notesRepository.save(any(Notes.class))).willAnswer(invocation -> invocation.getArgument(0));

		Notes notes = notesService.write("S-1", 1L, "Variables hold values", true);

		then(notesRepository).should().save(notes);
		assertThat(notes.getStudent()).isSameAs(student);
		assertThat(notes.getLecture()).isSameAs(lecture);
		assertThat(notes.getContent()).isEqualTo("Variables hold values");
		assertThat(notes.isPrivate()).isTrue();
		assertThat(notes.isReviewed()).isFalse();
	}

	@Test
	void writeRejectsStudentWhoDidNotAttend() {
		Student absent = new Student("S-2", "Grace", 2024);
		given(studentService.getByStudentId("S-2")).willReturn(absent);
		given(lectureService.getById(1L)).willReturn(lecture);

		assertThatThrownBy(() -> notesService.write("S-2", 1L, "Notes", false))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Student S-2 did not attend lecture 'Variables'");
		then(notesRepository).should(never()).save(any());
	}

	@Test
	void getByIdThrowsWhenMissing() {
		given(notesRepository.findById(7L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> notesService.getById(7L)).isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Notes '7' not found");
	}

	@Test
	void findByLectureDelegatesToRepository() {
		Notes notes = notes();
		given(lectureService.getById(1L)).willReturn(lecture);
		given(notesRepository.findByLecture(lecture)).willReturn(List.of(notes));

		assertThat(notesService.findByLecture(1L)).containsExactly(notes);
	}

	@Test
	void findByStudentDelegatesToRepository() {
		Notes notes = notes();
		given(studentService.getByStudentId("S-1")).willReturn(student);
		given(notesRepository.findByStudent(student)).willReturn(List.of(notes));

		assertThat(notesService.findByStudent("S-1")).containsExactly(notes);
	}

	@Test
	void findPendingReviewReturnsUnreviewedNotes() {
		Notes notes = notes();
		given(notesRepository.findByReviewerIsNull()).willReturn(List.of(notes));

		assertThat(notesService.findPendingReview()).containsExactly(notes);
	}

	@Test
	void editChangesContentAndVisibility() {
		Notes notes = stubNotes(7L);

		notesService.edit(7L, "Updated", true);

		assertThat(notes.getContent()).isEqualTo("Updated");
		assertThat(notes.isPrivate()).isTrue();
	}

	@Test
	void reviewRecordsReviewerGradeAndComment() {
		Notes notes = stubNotes(7L);
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);

		notesService.review(7L, "E-1", 95, "Excellent");

		assertThat(notes.getReviewer()).isSameAs(professor);
		assertThat(notes.getGrade()).isEqualTo(95);
		assertThat(notes.getReviewComment()).isEqualTo("Excellent");
	}

	@Test
	void reviewRejectsOutOfRangeGrade() {
		Notes notes = stubNotes(7L);
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);

		assertThatThrownBy(() -> notesService.review(7L, "E-1", 101, null))
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(notes.isReviewed()).isFalse();
	}

	@Test
	void reviewFailsWhenProfessorIsMissing() {
		Notes notes = stubNotes(7L);
		given(professorService.getByEmployeeId("NOPE")).willThrow(new ResourceNotFoundException("Professor", "NOPE"));

		assertThatThrownBy(() -> notesService.review(7L, "NOPE", 90, null))
			.isInstanceOf(ResourceNotFoundException.class);
		assertThat(notes.isReviewed()).isFalse();
	}

	@Test
	void deleteRemovesNotes() {
		Notes notes = stubNotes(7L);

		notesService.delete(7L);

		then(notesRepository).should().delete(notes);
	}

	private Notes notes() {
		return new Notes(student, lecture, "Variables hold values", false);
	}

	private Notes stubNotes(Long id) {
		Notes notes = notes();
		given(notesRepository.findById(id)).willReturn(Optional.of(notes));
		return notes;
	}

}
