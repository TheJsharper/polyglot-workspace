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
class ProfessorRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ProfessorRepository professorRepository;

	@Test
	void savesAndReloadsProfessor() {
		Professor saved = professorRepository.save(new Professor("E-1", "Ada", "Computer Science"));
		flushAndClear();

		Professor reloaded = professorRepository.findById(saved.getId()).orElseThrow();

		assertThat(reloaded.getEmployeeId()).isEqualTo("E-1");
		assertThat(reloaded.getName()).isEqualTo("Ada");
		assertThat(reloaded.getDepartment()).isEqualTo("Computer Science");
	}

	@Test
	void findByEmployeeIdReturnsMatchingProfessor() {
		professorRepository.save(new Professor("E-1", "Ada", "CS"));
		professorRepository.save(new Professor("E-2", "Grace", "Math"));
		flushAndClear();

		assertThat(professorRepository.findByEmployeeId("E-2"))
			.hasValueSatisfying(professor -> assertThat(professor.getName()).isEqualTo("Grace"));
	}

	@Test
	void findByEmployeeIdReturnsEmptyWhenMissing() {
		assertThat(professorRepository.findByEmployeeId("UNKNOWN")).isEmpty();
	}

	@Test
	void employeeIdMustBeUnique() {
		professorRepository.saveAndFlush(new Professor("E-1", "Ada", "CS"));

		assertThatThrownBy(() -> professorRepository.saveAndFlush(new Professor("E-1", "Duplicate", "Math")))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void loadsDeliveredLectures() {
		Professor professor = professorRepository.save(new Professor("E-1", "Ada", "CS"));
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
		new Lecture(course, professor, LocalDate.of(2026, 10, 15), LocalTime.of(9, 0), "Loops", "A-101");
		entityManager.persist(course);
		flushAndClear();

		assertThat(professorRepository.findByEmployeeId("E-1").orElseThrow().getLectures())
			.extracting(Lecture::getTopic)
			.containsExactlyInAnyOrder("Variables", "Loops");
	}

	@Test
	void professorDeliveringLecturesCannotBeDeleted() {
		Professor professor = professorRepository.save(new Professor("E-1", "Ada", "CS"));
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
		entityManager.persist(course);
		flushAndClear();

		professorRepository.deleteById(professor.getId());

		assertThatThrownBy(professorRepository::flush).isInstanceOf(DataIntegrityViolationException.class);
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}
