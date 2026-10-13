package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class CourseTest {

	private final Professor professor = new Professor("E-1", "Ada", "CS");

	@Test
	void constructorSetsFields() {
		Course course = new Course("CS101", "Programming", 6);

		assertThat(course.getId()).isNull();
		assertThat(course.getCourseCode()).isEqualTo("CS101");
		assertThat(course.getTitle()).isEqualTo("Programming");
		assertThat(course.getCredits()).isEqualTo(6);
		assertThat(course.getLectures()).isEmpty();
	}

	@Test
	void constructorRejectsMissingRequiredFields() {
		assertThatThrownBy(() -> new Course(null, "Programming", 6)).isInstanceOf(NullPointerException.class)
			.hasMessage("courseCode");
		assertThatThrownBy(() -> new Course("CS101", null, 6)).isInstanceOf(NullPointerException.class)
			.hasMessage("title");
	}

	@Test
	void settersUpdateMutableFields() {
		Course course = new Course("CS101", "Programming", 6);

		course.setTitle("Advanced Programming");
		course.setCredits(8);

		assertThat(course.getTitle()).isEqualTo("Advanced Programming");
		assertThat(course.getCredits()).isEqualTo(8);
	}

	@Test
	void creatingLectureRegistersItWithCourse() {
		Course course = new Course("CS101", "Programming", 6);

		Lecture lecture = lecture(course);

		assertThat(course.getLectures()).containsExactly(lecture);
	}

	@Test
	void lecturesViewIsReadOnly() {
		Course course = new Course("CS101", "Programming", 6);
		Lecture lecture = lecture(course);

		assertThatThrownBy(() -> course.getLectures().remove(lecture))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void removeLectureDetachesItFromProfessorAndAttendees() {
		Course course = new Course("CS101", "Programming", 6);
		Lecture lecture = lecture(course);
		Student student = new Student("S-1", "Alan", 2025);
		lecture.addAttendee(student);

		course.removeLecture(lecture);

		assertThat(course.getLectures()).isEmpty();
		assertThat(professor.getLectures()).isEmpty();
		assertThat(student.getAttendedLectures()).isEmpty();
		assertThat(lecture.getAttendees()).isEmpty();
	}

	@Test
	void removeLectureOfAnotherCourseHasNoEffect() {
		Course course = new Course("CS101", "Programming", 6);
		Course other = new Course("CS102", "Databases", 6);
		Lecture lecture = lecture(other);

		course.removeLecture(lecture);

		assertThat(other.getLectures()).containsExactly(lecture);
		assertThat(professor.getLectures()).containsExactly(lecture);
	}

	private Lecture lecture(Course course) {
		return new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
	}

}
