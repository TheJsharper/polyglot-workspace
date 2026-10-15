package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserAccountTest {

	@Test
	void studentAccountUsesStudentIdAsUsername() {
		Student student = new Student("S-1", "Alan", 2025);

		UserAccount account = UserAccount.forStudent(student, "hash");

		assertThat(account.getUsername()).isEqualTo("S-1");
		assertThat(account.getPasswordHash()).isEqualTo("hash");
		assertThat(account.getRole()).isEqualTo(Role.STUDENT);
		assertThat(account.getStudent()).isSameAs(student);
		assertThat(account.getProfessor()).isNull();
	}

	@Test
	void professorAccountUsesEmployeeIdAsUsername() {
		Professor professor = new Professor("E-1", "Ada", "CS");

		UserAccount account = UserAccount.forProfessor(professor, "hash");

		assertThat(account.getUsername()).isEqualTo("E-1");
		assertThat(account.getRole()).isEqualTo(Role.PROFESSOR);
		assertThat(account.getProfessor()).isSameAs(professor);
		assertThat(account.getStudent()).isNull();
	}

	@Test
	void adminAccountHasNoPerson() {
		UserAccount account = UserAccount.forAdmin("A-2", "hash");

		assertThat(account.getUsername()).isEqualTo("A-2");
		assertThat(account.getRole()).isEqualTo(Role.ADMIN);
		assertThat(account.getStudent()).isNull();
		assertThat(account.getProfessor()).isNull();
	}

	@Test
	void factoriesRejectMissingArguments() {
		assertThatThrownBy(() -> UserAccount.forStudent(null, "hash")).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> UserAccount.forProfessor(null, "hash")).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> UserAccount.forStudent(new Student("S-1", "Alan", 2025), null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void roleAuthorityHasSpringSecurityPrefix() {
		assertThat(Role.STUDENT.authority()).isEqualTo("ROLE_STUDENT");
		assertThat(Role.PROFESSOR.authority()).isEqualTo("ROLE_PROFESSOR");
		assertThat(Role.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
	}

}
