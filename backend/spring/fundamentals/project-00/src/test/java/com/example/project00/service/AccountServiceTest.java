package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Role;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private UserAccountRepository userAccountRepository;

	@Mock
	private StudentService studentService;

	@Mock
	private ProfessorService professorService;

	@Mock
	private PasswordEncoder passwordEncoder;

	private AccountService accountService;

	@BeforeEach
	void setUp() {
		accountService = new AccountService(userAccountRepository, studentService, professorService, passwordEncoder,
				"admin");
	}

	@Test
	void registerStudentEnrollsStudentAndStoresHashedPassword() {
		Student student = new Student("S-1", "Alan", 2025);
		given(studentService.enroll("S-1", "Alan", 2025)).willReturn(student);
		given(passwordEncoder.encode("secret-pass")).willReturn("{bcrypt}hash");

		Student registered = accountService.registerStudent("S-1", "Alan", 2025, "secret-pass");

		ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
		then(userAccountRepository).should().save(saved.capture());
		assertThat(registered).isSameAs(student);
		assertThat(saved.getValue().getUsername()).isEqualTo("S-1");
		assertThat(saved.getValue().getRole()).isEqualTo(Role.STUDENT);
		assertThat(saved.getValue().getPasswordHash()).isEqualTo("{bcrypt}hash");
		assertThat(saved.getValue().getStudent()).isSameAs(student);
	}

	@Test
	void registerAdminStoresHashedAdminAccount() {
		given(passwordEncoder.encode("secret-pass")).willReturn("{bcrypt}hash");
		given(userAccountRepository.save(org.mockito.ArgumentMatchers.any(UserAccount.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		UserAccount admin = accountService.registerAdmin("A-2", "secret-pass");

		assertThat(admin.getUsername()).isEqualTo("A-2");
		assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
		assertThat(admin.getPasswordHash()).isEqualTo("{bcrypt}hash");
	}

	@Test
	void registerProfessorHiresProfessorAndStoresHashedPassword() {
		Professor professor = new Professor("E-1", "Ada", "CS");
		given(professorService.hire("E-1", "Ada", "CS")).willReturn(professor);
		given(passwordEncoder.encode("secret-pass")).willReturn("{bcrypt}hash");

		Professor registered = accountService.registerProfessor("E-1", "Ada", "CS", "secret-pass");

		ArgumentCaptor<UserAccount> saved = ArgumentCaptor.forClass(UserAccount.class);
		then(userAccountRepository).should().save(saved.capture());
		assertThat(registered).isSameAs(professor);
		assertThat(saved.getValue().getUsername()).isEqualTo("E-1");
		assertThat(saved.getValue().getRole()).isEqualTo(Role.PROFESSOR);
		assertThat(saved.getValue().getProfessor()).isSameAs(professor);
	}

	@Test
	void registrationRejectsTakenUsername() {
		given(userAccountRepository.existsByUsername("S-1")).willReturn(true);

		assertThatThrownBy(() -> accountService.registerStudent("S-1", "Alan", 2025, "secret-pass"))
			.isInstanceOf(DuplicateResourceException.class)
			.hasMessage("Account 'S-1' already exists");
		then(studentService).should(never()).enroll(any(), any(), org.mockito.ArgumentMatchers.anyInt());
		then(userAccountRepository).should(never()).save(any());
	}

	@Test
	void registrationRejectsTheAdminUsernameIgnoringCase() {
		assertThatThrownBy(() -> accountService.registerProfessor("ADMIN", "Eve", "CS", "secret-pass"))
			.isInstanceOf(DuplicateResourceException.class);
		then(professorService).should(never()).hire(any(), any(), any());
	}

	@Test
	void registrationPropagatesDuplicateStudentIdWithoutCreatingAccount() {
		given(studentService.enroll("S-1", "Alan", 2025)).willThrow(new DuplicateResourceException("Student", "S-1"));

		assertThatThrownBy(() -> accountService.registerStudent("S-1", "Alan", 2025, "secret-pass"))
			.isInstanceOf(DuplicateResourceException.class);
		then(userAccountRepository).should(never()).save(any());
	}

}
