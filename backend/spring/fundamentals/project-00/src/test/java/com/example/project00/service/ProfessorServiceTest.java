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
import com.example.project00.repository.ProfessorRepository;
import com.example.project00.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

	@Mock
	private ProfessorRepository professorRepository;

	@Mock
	private UserAccountRepository userAccountRepository;

	@InjectMocks
	private ProfessorService professorService;

	@Test
	void hireSavesNewProfessor() {
		given(professorRepository.findByEmployeeId("E-1")).willReturn(Optional.empty());
		given(professorRepository.save(any(Professor.class))).willAnswer(invocation -> invocation.getArgument(0));

		Professor hired = professorService.hire("E-1", "Ada", "CS");

		then(professorRepository).should().save(hired);
		assertThat(hired.getEmployeeId()).isEqualTo("E-1");
		assertThat(hired.getName()).isEqualTo("Ada");
		assertThat(hired.getDepartment()).isEqualTo("CS");
	}

	@Test
	void hireRejectsDuplicateEmployeeId() {
		given(professorRepository.findByEmployeeId("E-1")).willReturn(Optional.of(new Professor("E-1", "Ada", "CS")));

		assertThatThrownBy(() -> professorService.hire("E-1", "Grace", "Math"))
			.isInstanceOf(DuplicateResourceException.class)
			.hasMessage("Professor 'E-1' already exists");
		then(professorRepository).should(never()).save(any());
	}

	@Test
	void getByEmployeeIdThrowsWhenMissing() {
		given(professorRepository.findByEmployeeId("NOPE")).willReturn(Optional.empty());

		assertThatThrownBy(() -> professorService.getByEmployeeId("NOPE"))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Professor 'NOPE' not found");
	}

	@Test
	void findAllDelegatesToRepository() {
		List<Professor> professors = List.of(new Professor("E-1", "Ada", "CS"));
		given(professorRepository.findAll()).willReturn(professors);

		assertThat(professorService.findAll()).isEqualTo(professors);
	}

	@Test
	void updateChangesNameAndDepartment() {
		Professor professor = new Professor("E-1", "Ada", "CS");
		given(professorRepository.findByEmployeeId("E-1")).willReturn(Optional.of(professor));

		professorService.update("E-1", "Ada Lovelace", "Mathematics");

		assertThat(professor.getName()).isEqualTo("Ada Lovelace");
		assertThat(professor.getDepartment()).isEqualTo("Mathematics");
	}

	@Test
	void deleteRemovesProfessorWithoutLecturesTogetherWithTeachingAndAccount() {
		Professor professor = new Professor("E-1", "Ada", "CS");
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		given(professorRepository.findByEmployeeId("E-1")).willReturn(Optional.of(professor));

		professorService.delete("E-1");

		assertThat(course.getTeachers()).isEmpty();
		assertThat(professor.getTaughtCourses()).isEmpty();
		then(userAccountRepository).should().deleteByProfessor(professor);
		then(professorRepository).should().delete(professor);
	}

	@Test
	void deleteRejectsProfessorWhoStillDeliversLectures() {
		Professor professor = new Professor("E-1", "Ada", "CS");
		Course course = new Course("CS101", "Programming", 6);
		course.assignTeacher(professor);
		new Lecture(course, professor, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
		given(professorRepository.findByEmployeeId("E-1")).willReturn(Optional.of(professor));

		assertThatThrownBy(() -> professorService.delete("E-1")).isInstanceOf(IllegalStateException.class)
			.hasMessage("Professor E-1 still delivers 1 lecture(s)");
		then(userAccountRepository).should(never()).deleteByProfessor(any());
		then(professorRepository).should(never()).delete(any());
	}

}
