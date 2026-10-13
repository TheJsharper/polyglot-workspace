package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ProfessorTest {

	@Test
	void constructorSetsFields() {
		Professor professor = new Professor("E-1", "Ada", "Computer Science");

		assertThat(professor.getId()).isNull();
		assertThat(professor.getEmployeeId()).isEqualTo("E-1");
		assertThat(professor.getName()).isEqualTo("Ada");
		assertThat(professor.getDepartment()).isEqualTo("Computer Science");
		assertThat(professor.getLectures()).isEmpty();
	}

	@Test
	void constructorRejectsMissingRequiredFields() {
		assertThatThrownBy(() -> new Professor(null, "Ada", "CS")).isInstanceOf(NullPointerException.class)
			.hasMessage("employeeId");
		assertThatThrownBy(() -> new Professor("E-1", null, "CS")).isInstanceOf(NullPointerException.class)
			.hasMessage("name");
		assertThatThrownBy(() -> new Professor("E-1", "Ada", null)).isInstanceOf(NullPointerException.class)
			.hasMessage("department");
	}

	@Test
	void settersUpdateMutableFields() {
		Professor professor = new Professor("E-1", "Ada", "CS");

		professor.setName("Ada Lovelace");
		professor.setDepartment("Mathematics");

		assertThat(professor.getName()).isEqualTo("Ada Lovelace");
		assertThat(professor.getDepartment()).isEqualTo("Mathematics");
	}

	@Test
	void deliveredLecturesAreTrackedAndReadOnly() {
		Professor professor = new Professor("E-1", "Ada", "CS");
		Lecture lecture = new Lecture(new Course("CS101", "Programming", 6), professor, LocalDate.of(2026, 10, 14),
				LocalTime.of(9, 0), "Variables", "A-101");

		assertThat(professor.getLectures()).containsExactly(lecture);
		assertThatThrownBy(() -> professor.getLectures().clear()).isInstanceOf(UnsupportedOperationException.class);
	}

}
