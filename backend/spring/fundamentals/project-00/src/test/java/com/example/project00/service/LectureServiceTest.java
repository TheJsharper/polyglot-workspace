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
import com.example.project00.repository.LectureRepository;
import com.example.project00.repository.NotesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

	private static final LocalDate DATE = LocalDate.of(2026, 10, 14);

	private static final LocalTime TIME = LocalTime.of(9, 0);

	@Mock
	private LectureRepository lectureRepository;

	@Mock
	private NotesRepository notesRepository;

	@Mock
	private CourseService courseService;

	@Mock
	private ProfessorService professorService;

	@Mock
	private StudentService studentService;

	@InjectMocks
	private LectureService lectureService;

	private Course course;

	private Professor professor;

	private Student student;

	@BeforeEach
	void setUp() {
		course = new Course("CS101", "Programming", 6);
		professor = new Professor("E-1", "Ada", "CS");
		student = new Student("S-1", "Alan", 2025);
	}

	@Test
	void scheduleCreatesLectureForCourseAndProfessor() {
		given(courseService.getByCode("CS101")).willReturn(course);
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);
		given(lectureRepository.save(any(Lecture.class))).willAnswer(invocation -> invocation.getArgument(0));

		Lecture lecture = lectureService.schedule("CS101", "E-1", DATE, TIME, "Variables", "A-101");

		then(lectureRepository).should().save(lecture);
		assertThat(lecture.getCourse()).isSameAs(course);
		assertThat(lecture.getProfessor()).isSameAs(professor);
		assertThat(lecture.getTopic()).isEqualTo("Variables");
		assertThat(course.getLectures()).containsExactly(lecture);
		assertThat(professor.getLectures()).containsExactly(lecture);
	}

	@Test
	void scheduleFailsWhenCourseIsMissing() {
		given(courseService.getByCode("NOPE")).willThrow(new ResourceNotFoundException("Course", "NOPE"));

		assertThatThrownBy(() -> lectureService.schedule("NOPE", "E-1", DATE, TIME, "Variables", "A-101"))
			.isInstanceOf(ResourceNotFoundException.class);
		then(lectureRepository).should(never()).save(any());
	}

	@Test
	void getByIdThrowsWhenMissing() {
		given(lectureRepository.findById(42L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> lectureService.getById(42L)).isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Lecture '42' not found");
	}

	@Test
	void findByCourseUsesChronologicalFinder() {
		Lecture lecture = lecture();
		given(courseService.getByCode("CS101")).willReturn(course);
		given(lectureRepository.findByCourseOrderByDateAscTimeAsc(course)).willReturn(List.of(lecture));

		assertThat(lectureService.findByCourse("CS101")).containsExactly(lecture);
	}

	@Test
	void findByProfessorDelegatesToRepository() {
		Lecture lecture = lecture();
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);
		given(lectureRepository.findByProfessor(professor)).willReturn(List.of(lecture));

		assertThat(lectureService.findByProfessor("E-1")).containsExactly(lecture);
	}

	@Test
	void rescheduleUpdatesDateTimeAndRoom() {
		Lecture lecture = stubLecture(1L);

		lectureService.reschedule(1L, DATE.plusDays(1), TIME.plusHours(2), "B-202");

		assertThat(lecture.getDate()).isEqualTo(DATE.plusDays(1));
		assertThat(lecture.getTime()).isEqualTo(TIME.plusHours(2));
		assertThat(lecture.getRoom()).isEqualTo("B-202");
	}

	@Test
	void assignProfessorMovesLectureToNewProfessor() {
		Lecture lecture = stubLecture(1L);
		Professor replacement = new Professor("E-2", "Grace", "CS");
		given(professorService.getByEmployeeId("E-2")).willReturn(replacement);

		lectureService.assignProfessor(1L, "E-2");

		assertThat(lecture.getProfessor()).isSameAs(replacement);
		assertThat(professor.getLectures()).isEmpty();
		assertThat(replacement.getLectures()).containsExactly(lecture);
	}

	@Test
	void recordAttendanceAddsStudent() {
		Lecture lecture = stubLecture(1L);
		given(studentService.getByStudentId("S-1")).willReturn(student);

		lectureService.recordAttendance(1L, "S-1");

		assertThat(lecture.hasAttendee(student)).isTrue();
		assertThat(student.getAttendedLectures()).containsExactly(lecture);
	}

	@Test
	void removeAttendanceRemovesStudentWithoutNotes() {
		Lecture lecture = stubLecture(1L);
		lecture.addAttendee(student);
		given(studentService.getByStudentId("S-1")).willReturn(student);
		given(notesRepository.findByLecture(lecture)).willReturn(List.of());

		lectureService.removeAttendance(1L, "S-1");

		assertThat(lecture.hasAttendee(student)).isFalse();
		assertThat(student.getAttendedLectures()).isEmpty();
	}

	@Test
	void removeAttendanceRejectsStudentWithNotesForLecture() {
		Lecture lecture = stubLecture(1L);
		lecture.addAttendee(student);
		given(studentService.getByStudentId("S-1")).willReturn(student);
		given(notesRepository.findByLecture(lecture)).willReturn(List.of(new Notes(student, lecture, "Notes", false)));

		assertThatThrownBy(() -> lectureService.removeAttendance(1L, "S-1")).isInstanceOf(IllegalStateException.class)
			.hasMessage("Student S-1 has notes for lecture 1 and cannot be removed");
		assertThat(lecture.hasAttendee(student)).isTrue();
	}

	@Test
	void cancelDetachesAndDeletesLectureWithoutNotes() {
		Lecture lecture = stubLecture(1L);
		lecture.addAttendee(student);
		given(notesRepository.findByLecture(lecture)).willReturn(List.of());

		lectureService.cancel(1L);

		assertThat(course.getLectures()).isEmpty();
		assertThat(professor.getLectures()).isEmpty();
		assertThat(student.getAttendedLectures()).isEmpty();
		then(lectureRepository).should().delete(lecture);
	}

	@Test
	void cancelRejectsLectureWithNotes() {
		Lecture lecture = stubLecture(1L);
		lecture.addAttendee(student);
		given(notesRepository.findByLecture(lecture)).willReturn(List.of(new Notes(student, lecture, "Notes", false)));

		assertThatThrownBy(() -> lectureService.cancel(1L)).isInstanceOf(IllegalStateException.class)
			.hasMessage("Lecture 1 has notes and cannot be cancelled");
		assertThat(course.getLectures()).containsExactly(lecture);
		then(lectureRepository).should(never()).delete(any());
	}

	private Lecture lecture() {
		return new Lecture(course, professor, DATE, TIME, "Variables", "A-101");
	}

	private Lecture stubLecture(Long id) {
		Lecture lecture = lecture();
		given(lectureRepository.findById(id)).willReturn(Optional.of(lecture));
		return lecture;
	}

}
