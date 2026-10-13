package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LectureTest {

	private static final LocalDate DATE = LocalDate.of(2026, 10, 14);

	private static final LocalTime TIME = LocalTime.of(9, 0);

	private Course course;

	private Professor professor;

	private Student student;

	private Lecture lecture;

	@BeforeEach
	void setUp() {
		course = new Course("CS101", "Programming", 6);
		professor = new Professor("E-1", "Ada", "CS");
		student = new Student("S-1", "Alan", 2025);
		lecture = new Lecture(course, professor, DATE, TIME, "Variables", "A-101");
	}

	@Test
	void constructorSetsFieldsAndRegistersWithCourseAndProfessor() {
		assertThat(lecture.getCourse()).isSameAs(course);
		assertThat(lecture.getProfessor()).isSameAs(professor);
		assertThat(lecture.getDate()).isEqualTo(DATE);
		assertThat(lecture.getTime()).isEqualTo(TIME);
		assertThat(lecture.getTopic()).isEqualTo("Variables");
		assertThat(lecture.getRoom()).isEqualTo("A-101");
		assertThat(lecture.getAttendees()).isEmpty();
		assertThat(course.getLectures()).containsExactly(lecture);
		assertThat(professor.getLectures()).containsExactly(lecture);
	}

	@Test
	void constructorRejectsMissingRequiredFields() {
		assertThatThrownBy(() -> new Lecture(null, professor, DATE, TIME, "T", "R")).hasMessage("course");
		assertThatThrownBy(() -> new Lecture(course, null, DATE, TIME, "T", "R")).hasMessage("professor");
		assertThatThrownBy(() -> new Lecture(course, professor, null, TIME, "T", "R")).hasMessage("date");
		assertThatThrownBy(() -> new Lecture(course, professor, DATE, null, "T", "R")).hasMessage("time");
		assertThatThrownBy(() -> new Lecture(course, professor, DATE, TIME, null, "R")).hasMessage("topic");
		assertThatThrownBy(() -> new Lecture(course, professor, DATE, TIME, "T", null)).hasMessage("room");
	}

	@Test
	void rejectedConstructionDoesNotRegisterLecture() {
		Course otherCourse = new Course("CS102", "Databases", 6);

		assertThatThrownBy(() -> new Lecture(otherCourse, professor, DATE, TIME, null, "R"))
			.isInstanceOf(NullPointerException.class);

		assertThat(otherCourse.getLectures()).isEmpty();
		assertThat(professor.getLectures()).containsExactly(lecture);
	}

	@Test
	void settersUpdateSchedulingFields() {
		lecture.setDate(DATE.plusDays(1));
		lecture.setTime(TIME.plusHours(2));
		lecture.setTopic("Loops");
		lecture.setRoom("B-202");

		assertThat(lecture.getDate()).isEqualTo(DATE.plusDays(1));
		assertThat(lecture.getTime()).isEqualTo(TIME.plusHours(2));
		assertThat(lecture.getTopic()).isEqualTo("Loops");
		assertThat(lecture.getRoom()).isEqualTo("B-202");
	}

	@Test
	void addAttendeeUpdatesBothSides() {
		lecture.addAttendee(student);

		assertThat(lecture.getAttendees()).containsExactly(student);
		assertThat(student.getAttendedLectures()).containsExactly(lecture);
		assertThat(lecture.hasAttendee(student)).isTrue();
	}

	@Test
	void addingSameAttendeeTwiceIsIdempotent() {
		lecture.addAttendee(student);
		lecture.addAttendee(student);

		assertThat(lecture.getAttendees()).hasSize(1);
		assertThat(student.getAttendedLectures()).hasSize(1);
	}

	@Test
	void addAttendeeRejectsNull() {
		assertThatThrownBy(() -> lecture.addAttendee(null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void removeAttendeeUpdatesBothSides() {
		lecture.addAttendee(student);

		lecture.removeAttendee(student);

		assertThat(lecture.getAttendees()).isEmpty();
		assertThat(student.getAttendedLectures()).isEmpty();
		assertThat(lecture.hasAttendee(student)).isFalse();
	}

	@Test
	void removingNonAttendeeHasNoEffect() {
		Student other = new Student("S-2", "Grace", 2024);
		lecture.addAttendee(student);

		lecture.removeAttendee(other);

		assertThat(lecture.getAttendees()).containsExactly(student);
	}

	@Test
	void attendeesViewIsReadOnly() {
		assertThatThrownBy(() -> lecture.getAttendees().add(student))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void assignProfessorMovesLectureBetweenProfessors() {
		Professor replacement = new Professor("E-2", "Grace", "CS");

		lecture.assignProfessor(replacement);

		assertThat(lecture.getProfessor()).isSameAs(replacement);
		assertThat(replacement.getLectures()).containsExactly(lecture);
		assertThat(professor.getLectures()).isEmpty();
	}

	@Test
	void assignProfessorRejectsNullAndKeepsCurrentProfessor() {
		assertThatThrownBy(() -> lecture.assignProfessor(null)).isInstanceOf(NullPointerException.class);

		assertThat(lecture.getProfessor()).isSameAs(professor);
		assertThat(professor.getLectures()).containsExactly(lecture);
	}

}
