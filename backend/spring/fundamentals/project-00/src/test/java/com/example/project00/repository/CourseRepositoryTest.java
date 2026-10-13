package com.example.project00.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class CourseRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CourseRepository courseRepository;

	@Test
	void savesAndReloadsCourse() {
		Course saved = courseRepository.save(new Course("CS101", "Programming", 6));
		flushAndClear();

		Course reloaded = courseRepository.findById(saved.getId()).orElseThrow();

		assertThat(reloaded.getId()).isNotNull();
		assertThat(reloaded.getCourseCode()).isEqualTo("CS101");
		assertThat(reloaded.getTitle()).isEqualTo("Programming");
		assertThat(reloaded.getCredits()).isEqualTo(6);
	}

	@Test
	void findByCourseCodeReturnsMatchingCourse() {
		courseRepository.save(new Course("CS101", "Programming", 6));
		courseRepository.save(new Course("CS102", "Databases", 4));
		flushAndClear();

		assertThat(courseRepository.findByCourseCode("CS102")).hasValueSatisfying(
				course -> assertThat(course.getTitle()).isEqualTo("Databases"));
	}

	@Test
	void findByCourseCodeReturnsEmptyWhenMissing() {
		assertThat(courseRepository.findByCourseCode("UNKNOWN")).isEmpty();
	}

	@Test
	void courseCodeMustBeUnique() {
		courseRepository.saveAndFlush(new Course("CS101", "Programming", 6));

		assertThatThrownBy(() -> courseRepository.saveAndFlush(new Course("CS101", "Duplicate", 3)))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void savingCourseCascadesToLectures() {
		Professor professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");

		courseRepository.save(course);
		flushAndClear();

		assertThat(courseRepository.findByCourseCode("CS101").orElseThrow().getLectures())
			.extracting(Lecture::getTopic)
			.containsExactly("Variables");
	}

	@Test
	void removingLectureFromCourseDeletesIt() {
		Professor professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		Lecture lecture = new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables",
				"A-101");
		courseRepository.saveAndFlush(course);

		course.removeLecture(lecture);
		flushAndClear();

		assertThat(entityManager.find(Lecture.class, lecture.getId())).isNull();
		assertThat(courseRepository.findByCourseCode("CS101").orElseThrow().getLectures()).isEmpty();
	}

	@Test
	void deletingCourseDeletesItsLectures() {
		Professor professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		Lecture lecture = new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables",
				"A-101");
		courseRepository.saveAndFlush(course);
		flushAndClear();

		courseRepository.deleteById(course.getId());
		flushAndClear();

		assertThat(courseRepository.count()).isZero();
		assertThat(entityManager.find(Lecture.class, lecture.getId())).isNull();
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}
