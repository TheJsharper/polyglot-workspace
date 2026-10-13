package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private StudentService studentService;

	@Mock
	private ProfessorService professorService;

	@InjectMocks
	private CourseService courseService;

	@Test
	void createSavesNewCourse() {
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.empty());
		given(courseRepository.save(any(Course.class))).willAnswer(invocation -> invocation.getArgument(0));

		Course created = courseService.create("CS101", "Programming", 6);

		ArgumentCaptor<Course> saved = ArgumentCaptor.forClass(Course.class);
		then(courseRepository).should().save(saved.capture());
		assertThat(created).isSameAs(saved.getValue());
		assertThat(created.getCourseCode()).isEqualTo("CS101");
		assertThat(created.getTitle()).isEqualTo("Programming");
		assertThat(created.getCredits()).isEqualTo(6);
	}

	@Test
	void createRejectsDuplicateCourseCode() {
		given(courseRepository.findByCourseCode("CS101"))
			.willReturn(Optional.of(new Course("CS101", "Programming", 6)));

		assertThatThrownBy(() -> courseService.create("CS101", "Other", 3))
			.isInstanceOf(DuplicateResourceException.class)
			.hasMessage("Course 'CS101' already exists");
		then(courseRepository).should(never()).save(any());
	}

	@Test
	void getByCodeReturnsCourse() {
		Course course = new Course("CS101", "Programming", 6);
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));

		assertThat(courseService.getByCode("CS101")).isSameAs(course);
	}

	@Test
	void getByCodeThrowsWhenMissing() {
		given(courseRepository.findByCourseCode("NOPE")).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseService.getByCode("NOPE")).isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Course 'NOPE' not found");
	}

	@Test
	void findAllDelegatesToRepository() {
		List<Course> courses = List.of(new Course("CS101", "Programming", 6), new Course("CS102", "Databases", 4));
		given(courseRepository.findAll()).willReturn(courses);

		assertThat(courseService.findAll()).isEqualTo(courses);
	}

	@Test
	void updateChangesTitleAndCredits() {
		Course course = new Course("CS101", "Programming", 6);
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));

		Course updated = courseService.update("CS101", "Programming I", 8);

		assertThat(updated).isSameAs(course);
		assertThat(course.getTitle()).isEqualTo("Programming I");
		assertThat(course.getCredits()).isEqualTo(8);
	}

	@Test
	void deleteRemovesCourse() {
		Course course = new Course("CS101", "Programming", 6);
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));

		courseService.delete("CS101");

		then(courseRepository).should().delete(course);
	}

	@Test
	void deleteThrowsWhenMissing() {
		given(courseRepository.findByCourseCode("NOPE")).willReturn(Optional.empty());

		assertThatThrownBy(() -> courseService.delete("NOPE")).isInstanceOf(ResourceNotFoundException.class);
		then(courseRepository).should(never()).delete(any());
	}

	@Test
	void enrollAddsStudentToCourse() {
		Course course = new Course("CS101", "Programming", 6);
		Student student = new Student("S-1", "Alan", 2025);
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(studentService.getByStudentId("S-1")).willReturn(student);

		Course enrolled = courseService.enroll("CS101", "S-1");

		assertThat(enrolled).isSameAs(course);
		assertThat(course.isEnrolled(student)).isTrue();
	}

	@Test
	void enrollPropagatesMissingStudent() {
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(new Course("CS101", "Programming", 6)));
		given(studentService.getByStudentId("NOPE")).willThrow(new ResourceNotFoundException("Student", "NOPE"));

		assertThatThrownBy(() -> courseService.enroll("CS101", "NOPE")).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void unenrollRemovesStudentFromCourse() {
		Course course = new Course("CS101", "Programming", 6);
		Student student = new Student("S-1", "Alan", 2025);
		course.enroll(student);
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(studentService.getByStudentId("S-1")).willReturn(student);

		courseService.unenroll("CS101", "S-1");

		assertThat(course.isEnrolled(student)).isFalse();
	}

	@Test
	void assignTeacherAddsProfessorToCourse() {
		Course course = new Course("CS101", "Programming", 6);
		Professor professor = new Professor("E-1", "Ada", "CS");
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);

		Course updated = courseService.assignTeacher("CS101", "E-1");

		assertThat(updated.isTaughtBy(professor)).isTrue();
	}

	@Test
	void unassignTeacherRefusesProfessorWhoDeliversLectures() {
		Course course = new Course("CS101", "Programming", 6);
		Professor professor = new Professor("E-1", "Ada", "CS");
		course.assignTeacher(professor);
		new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);

		assertThatThrownBy(() -> courseService.unassignTeacher("CS101", "E-1"))
			.isInstanceOf(IllegalStateException.class);
		assertThat(course.isTaughtBy(professor)).isTrue();
	}

	@Test
	void unassignTeacherRemovesProfessorWithoutLectures() {
		Course course = new Course("CS101", "Programming", 6);
		Professor professor = new Professor("E-1", "Ada", "CS");
		course.assignTeacher(professor);
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(professorService.getByEmployeeId("E-1")).willReturn(professor);

		courseService.unassignTeacher("CS101", "E-1");

		assertThat(course.isTaughtBy(professor)).isFalse();
	}

}
