package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class StudentTest {

	@Test
	void constructorSetsFields() {
		Student student = new Student("S-1", "Alan", 2025);

		assertThat(student.getId()).isNull();
		assertThat(student.getStudentId()).isEqualTo("S-1");
		assertThat(student.getName()).isEqualTo("Alan");
		assertThat(student.getEnrollmentYear()).isEqualTo(2025);
		assertThat(student.getAttendedLectures()).isEmpty();
	}

	@Test
	void constructorRejectsMissingRequiredFields() {
		assertThatThrownBy(() -> new Student(null, "Alan", 2025)).isInstanceOf(NullPointerException.class)
			.hasMessage("studentId");
		assertThatThrownBy(() -> new Student("S-1", null, 2025)).isInstanceOf(NullPointerException.class)
			.hasMessage("name");
	}

	@Test
	void settersUpdateMutableFields() {
		Student student = new Student("S-1", "Alan", 2025);

		student.setName("Alan Turing");
		student.setEnrollmentYear(2026);

		assertThat(student.getName()).isEqualTo("Alan Turing");
		assertThat(student.getEnrollmentYear()).isEqualTo(2026);
	}

	@Test
	void attendedLecturesAreTrackedAndReadOnly() {
		Student student = new Student("S-1", "Alan", 2025);
		Lecture lecture = new Lecture(new Course("CS101", "Programming", 6), new Professor("E-1", "Ada", "CS"),
				LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");

		lecture.addAttendee(student);

		assertThat(student.getAttendedLectures()).containsExactly(lecture);
		assertThatThrownBy(() -> student.getAttendedLectures().clear())
			.isInstanceOf(UnsupportedOperationException.class);
	}

}
