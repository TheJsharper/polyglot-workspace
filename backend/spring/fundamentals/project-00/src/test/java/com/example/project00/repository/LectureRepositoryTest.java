package com.example.project00.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class LectureRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private LectureRepository lectureRepository;

	private Course course;

	private Professor professor;

	private Student student;

	@BeforeEach
	void setUp() {
		professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		student = entityManager.persist(new Student("S-1", "Alan", 2025));
		course = entityManager.persist(new Course("CS101", "Programming", 6));
	}

	@Test
	void savesAndReloadsLectureWithAssociations() {
		Lecture saved = lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables"));
		flushAndClear();

		Lecture reloaded = lectureRepository.findById(saved.getId()).orElseThrow();

		assertThat(reloaded.getDate()).isEqualTo(LocalDate.of(2026, 10, 14));
		assertThat(reloaded.getTime()).isEqualTo(LocalTime.of(9, 0));
		assertThat(reloaded.getTopic()).isEqualTo("Variables");
		assertThat(reloaded.getRoom()).isEqualTo("A-101");
		assertThat(reloaded.getCourse().getCourseCode()).isEqualTo("CS101");
		assertThat(reloaded.getProfessor().getEmployeeId()).isEqualTo("E-1");
	}

	@Test
	void persistsAttendanceInJoinTable() {
		Lecture lecture = lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables"));
		lecture.addAttendee(student);
		flushAndClear();

		assertThat(lectureRepository.findById(lecture.getId()).orElseThrow().getAttendees())
			.extracting(Student::getStudentId)
			.containsExactly("S-1");
		assertThat(attendanceRows()).isEqualTo(1);
	}

	@Test
	void removingAttendeeDeletesJoinTableRow() {
		Lecture lecture = lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables"));
		lecture.addAttendee(student);
		entityManager.flush();

		lecture.removeAttendee(student);
		flushAndClear();

		assertThat(lectureRepository.findById(lecture.getId()).orElseThrow().getAttendees()).isEmpty();
		assertThat(attendanceRows()).isZero();
	}

	@Test
	void assigningNewProfessorIsPersisted() {
		Professor replacement = entityManager.persist(new Professor("E-2", "Grace", "CS"));
		Lecture lecture = lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables"));

		lecture.assignProfessor(replacement);
		flushAndClear();

		assertThat(lectureRepository.findById(lecture.getId()).orElseThrow().getProfessor().getEmployeeId())
			.isEqualTo("E-2");
	}

	@Test
	void findByCourseReturnsOnlyThatCoursesLecturesInChronologicalOrder() {
		Course otherCourse = entityManager.persist(new Course("CS102", "Databases", 4));
		lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables"));
		lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(8, 0), "Intro"));
		lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 13), LocalTime.of(10, 0), "Setup"));
		lectureRepository.save(lecture(otherCourse, LocalDate.of(2026, 10, 12), LocalTime.of(8, 0), "SQL"));
		flushAndClear();

		assertThat(lectureRepository.findByCourseOrderByDateAscTimeAsc(course))
			.extracting(Lecture::getTopic)
			.containsExactly("Setup", "Intro", "Variables");
	}

	@Test
	void findByCourseReturnsEmptyListWhenCourseHasNoLectures() {
		assertThat(lectureRepository.findByCourseOrderByDateAscTimeAsc(course)).isEmpty();
	}

	@Test
	void findByProfessorReturnsOnlyTheirLectures() {
		Professor other = entityManager.persist(new Professor("E-2", "Grace", "CS"));
		lectureRepository.save(lecture(course, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables"));
		lectureRepository.save(new Lecture(course, other, LocalDate.of(2026, 10, 15), LocalTime.of(9, 0), "Loops", "B-1"));
		flushAndClear();

		assertThat(lectureRepository.findByProfessor(professor))
			.extracting(Lecture::getTopic)
			.containsExactly("Variables");
	}

	private Lecture lecture(Course course, LocalDate date, LocalTime time, String topic) {
		return new Lecture(course, professor, date, time, topic, "A-101");
	}

	private long attendanceRows() {
		return ((Number) entityManager.getEntityManager()
			.createNativeQuery("SELECT COUNT(*) FROM lecture_attendance")
			.getSingleResult()).longValue();
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}
