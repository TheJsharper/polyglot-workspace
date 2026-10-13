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
		course.enroll(student);
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

	@Test
	void newCourseHasNoTeachersOrStudents() {
		Course course = new Course("CS101", "Programming", 6);

		assertThat(course.getTeachers()).isEmpty();
		assertThat(course.getEnrolledStudents()).isEmpty();
	}

	@Test
	void enrollUpdatesBothSidesAndIsIdempotent() {
		Course course = new Course("CS101", "Programming", 6);
		Student student = new Student("S-1", "Alan", 2025);

		course.enroll(student);
		course.enroll(student);

		assertThat(course.getEnrolledStudents()).containsExactly(student);
		assertThat(student.getEnrolledCourses()).containsExactly(course);
		assertThat(course.isEnrolled(student)).isTrue();
	}

	@Test
	void unenrollUpdatesBothSides() {
		Course course = new Course("CS101", "Programming", 6);
		Student student = new Student("S-1", "Alan", 2025);
		course.enroll(student);

		course.unenroll(student);

		assertThat(course.getEnrolledStudents()).isEmpty();
		assertThat(student.getEnrolledCourses()).isEmpty();
		assertThat(course.isEnrolled(student)).isFalse();
	}

	@Test
	void unenrollIsRefusedWhileStudentAttendsALecture() {
		Course course = new Course("CS101", "Programming", 6);
		Student student = new Student("S-1", "Alan", 2025);
		course.enroll(student);
		lecture(course).addAttendee(student);

		assertThatThrownBy(() -> course.unenroll(student)).isInstanceOf(IllegalStateException.class);
		assertThat(course.isEnrolled(student)).isTrue();
	}

	@Test
	void assignTeacherUpdatesBothSidesAndIsIdempotent() {
		Course course = new Course("CS101", "Programming", 6);

		course.assignTeacher(professor);
		course.assignTeacher(professor);

		assertThat(course.getTeachers()).containsExactly(professor);
		assertThat(professor.getTaughtCourses()).containsExactly(course);
		assertThat(course.isTaughtBy(professor)).isTrue();
	}

	@Test
	void unassignTeacherUpdatesBothSides() {
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);

		course.unassignTeacher(professor);

		assertThat(course.getTeachers()).isEmpty();
		assertThat(professor.getTaughtCourses()).isEmpty();
		assertThat(course.isTaughtBy(professor)).isFalse();
	}

	@Test
	void unassignTeacherIsRefusedWhileProfessorDeliversALecture() {
		Course course = new Course("CS101", "Programming", 6);
		lecture(course);

		assertThatThrownBy(() -> course.unassignTeacher(professor)).isInstanceOf(IllegalStateException.class);
		assertThat(course.isTaughtBy(professor)).isTrue();
	}

	@Test
	void teacherAndStudentViewsAreReadOnly() {
		Course course = new Course("CS101", "Programming", 6);

		assertThatThrownBy(() -> course.getTeachers().add(professor))
			.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> course.getEnrolledStudents().add(new Student("S-1", "Alan", 2025)))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	private Lecture lecture(Course course) {
		course.assignTeacher(professor);
		return new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
	}

}
