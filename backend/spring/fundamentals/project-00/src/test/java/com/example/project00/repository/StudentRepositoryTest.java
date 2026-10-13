package com.example.project00.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class StudentRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private StudentRepository studentRepository;

	@Test
	void savesAndReloadsStudent() {
		Student saved = studentRepository.save(new Student("S-1", "Alan", 2025));
		flushAndClear();

		Student reloaded = studentRepository.findById(saved.getId()).orElseThrow();

		assertThat(reloaded.getStudentId()).isEqualTo("S-1");
		assertThat(reloaded.getName()).isEqualTo("Alan");
		assertThat(reloaded.getEnrollmentYear()).isEqualTo(2025);
	}

	@Test
	void findByStudentIdReturnsMatchingStudent() {
		studentRepository.save(new Student("S-1", "Alan", 2025));
		studentRepository.save(new Student("S-2", "Grace", 2024));
		flushAndClear();

		assertThat(studentRepository.findByStudentId("S-2"))
			.hasValueSatisfying(student -> assertThat(student.getName()).isEqualTo("Grace"));
	}

	@Test
	void findByStudentIdReturnsEmptyWhenMissing() {
		assertThat(studentRepository.findByStudentId("UNKNOWN")).isEmpty();
	}

	@Test
	void studentIdMustBeUnique() {
		studentRepository.saveAndFlush(new Student("S-1", "Alan", 2025));

		assertThatThrownBy(() -> studentRepository.saveAndFlush(new Student("S-1", "Duplicate", 2025)))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void loadsAttendedLecturesFromInverseSide() {
		Student student = studentRepository.save(new Student("S-1", "Alan", 2025));
		Professor professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		course.enroll(student);
		Lecture lecture = new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables",
				"A-101");
		lecture.addAttendee(student);
		entityManager.persist(course);
		flushAndClear();

		assertThat(studentRepository.findByStudentId("S-1").orElseThrow().getAttendedLectures())
			.extracting(Lecture::getTopic)
			.containsExactly("Variables");
	}

	@Test
	void persistsEnrollmentInJoinTableAndLoadsItFromInverseSide() {
		Student student = studentRepository.save(new Student("S-1", "Alan", 2025));
		Course course = new Course("CS101", "Programming", 6);
		course.enroll(student);
		entityManager.persist(course);
		flushAndClear();

		assertThat(studentRepository.findByStudentId("S-1").orElseThrow().getEnrolledCourses())
			.extracting(Course::getCourseCode)
			.containsExactly("CS101");
		assertThat(((Number) entityManager.getEntityManager()
			.createNativeQuery("SELECT COUNT(*) FROM course_enrollment")
			.getSingleResult()).longValue()).isEqualTo(1);
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}
