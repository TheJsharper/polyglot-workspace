package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import com.example.project00.domain.Course;
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

}
