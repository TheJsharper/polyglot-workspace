package com.example.project00.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class NotesRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private NotesRepository notesRepository;

	@Autowired
	private LectureRepository lectureRepository;

	private Professor professor;

	private Student alan;

	private Student grace;

	private Course course;

	private Lecture variables;

	private Lecture loops;

	@BeforeEach
	void setUp() {
		professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		alan = entityManager.persist(new Student("S-1", "Alan", 2025));
		grace = entityManager.persist(new Student("S-2", "Grace", 2024));
		course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		course.enroll(alan);
		course.enroll(grace);
		variables = new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
		loops = new Lecture(course, professor, LocalDate.of(2026, 10, 15), LocalTime.of(9, 0), "Loops", "A-101");
		variables.addAttendee(alan);
		variables.addAttendee(grace);
		loops.addAttendee(alan);
		entityManager.persist(course);
	}

	@Test
	void savesAndReloadsNotes() {
		Notes saved = notesRepository.save(new Notes(alan, variables, "Variables hold values", true));
		flushAndClear();

		Notes reloaded = notesRepository.findById(saved.getId()).orElseThrow();

		assertThat(reloaded.getContent()).isEqualTo("Variables hold values");
		assertThat(reloaded.isPrivate()).isTrue();
		assertThat(reloaded.getTimestamp()).isCloseTo(saved.getTimestamp(), within(1, ChronoUnit.MICROS));
		assertThat(reloaded.getStudent().getStudentId()).isEqualTo("S-1");
		assertThat(reloaded.getLecture().getTopic()).isEqualTo("Variables");
		assertThat(reloaded.isReviewed()).isFalse();
		assertThat(reloaded.getGrade()).isNull();
	}

	@Test
	void persistsReview() {
		Notes notes = notesRepository.save(new Notes(alan, variables, "Loops repeat work", false));

		notes.review(professor, 92, "Clear and concise");
		flushAndClear();

		Notes reloaded = notesRepository.findById(notes.getId()).orElseThrow();
		assertThat(reloaded.isReviewed()).isTrue();
		assertThat(reloaded.getReviewer().getEmployeeId()).isEqualTo("E-1");
		assertThat(reloaded.getGrade()).isEqualTo(92);
		assertThat(reloaded.getReviewComment()).isEqualTo("Clear and concise");
	}

	@Test
	void findByLectureReturnsOnlyThatLecturesNotes() {
		notesRepository.save(new Notes(alan, variables, "Alan on variables", false));
		notesRepository.save(new Notes(grace, variables, "Grace on variables", false));
		notesRepository.save(new Notes(alan, loops, "Alan on loops", false));
		flushAndClear();

		assertThat(notesRepository.findByLecture(variables))
			.extracting(Notes::getContent)
			.containsExactlyInAnyOrder("Alan on variables", "Grace on variables");
	}

	@Test
	void findByStudentReturnsOnlyThatStudentsNotes() {
		notesRepository.save(new Notes(alan, variables, "Alan on variables", false));
		notesRepository.save(new Notes(grace, variables, "Grace on variables", false));
		notesRepository.save(new Notes(alan, loops, "Alan on loops", false));
		flushAndClear();

		assertThat(notesRepository.findByStudent(alan))
			.extracting(Notes::getContent)
			.containsExactlyInAnyOrder("Alan on variables", "Alan on loops");
		assertThat(notesRepository.findByStudent(entityManager.persist(new Student("S-3", "Edsger", 2023))))
			.isEmpty();
	}

	@Test
	void findByReviewerIsNullReturnsOnlyUnreviewedNotes() {
		Notes reviewed = notesRepository.save(new Notes(alan, variables, "Reviewed", false));
		notesRepository.save(new Notes(grace, variables, "Pending", false));
		reviewed.review(professor, 80, null);
		flushAndClear();

		assertThat(notesRepository.findByReviewerIsNull())
			.extracting(Notes::getContent)
			.containsExactly("Pending");
	}

	@Test
	void lectureWithNotesCannotBeDeleted() {
		notesRepository.save(new Notes(alan, variables, "Keep me", false));
		flushAndClear();

		lectureRepository.deleteById(variables.getId());

		assertThatThrownBy(lectureRepository::flush).isInstanceOf(DataIntegrityViolationException.class);
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}
