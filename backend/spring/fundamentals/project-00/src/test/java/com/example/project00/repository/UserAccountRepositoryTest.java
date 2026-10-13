package com.example.project00.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Role;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class UserAccountRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Test
	void savesAndFindsAccountByUsername() {
		Student student = entityManager.persist(new Student("S-1", "Alan", 2025));
		userAccountRepository.save(UserAccount.forStudent(student, "hash"));
		flushAndClear();

		assertThat(userAccountRepository.findByUsername("S-1")).hasValueSatisfying(account -> {
			assertThat(account.getRole()).isEqualTo(Role.STUDENT);
			assertThat(account.getPasswordHash()).isEqualTo("hash");
			assertThat(account.getStudent().getName()).isEqualTo("Alan");
		});
		assertThat(userAccountRepository.findByUsername("UNKNOWN")).isEmpty();
	}

	@Test
	void existsByUsernameReportsTakenNames() {
		Professor professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		userAccountRepository.save(UserAccount.forProfessor(professor, "hash"));
		flushAndClear();

		assertThat(userAccountRepository.existsByUsername("E-1")).isTrue();
		assertThat(userAccountRepository.existsByUsername("E-2")).isFalse();
	}

	@Test
	void usernameMustBeUnique() {
		Student student = entityManager.persist(new Student("X-1", "Alan", 2025));
		Professor professor = entityManager.persist(new Professor("X-1", "Ada", "CS"));
		userAccountRepository.saveAndFlush(UserAccount.forStudent(student, "hash"));

		assertThatThrownBy(() -> userAccountRepository.saveAndFlush(UserAccount.forProfessor(professor, "hash")))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void deleteByStudentAndByProfessorRemoveOnlyTheirAccount() {
		Student student = entityManager.persist(new Student("S-1", "Alan", 2025));
		Professor professor = entityManager.persist(new Professor("E-1", "Ada", "CS"));
		userAccountRepository.save(UserAccount.forStudent(student, "hash"));
		userAccountRepository.save(UserAccount.forProfessor(professor, "hash"));
		entityManager.flush();

		userAccountRepository.deleteByStudent(student);
		entityManager.flush();
		assertThat(userAccountRepository.findAll()).extracting(UserAccount::getUsername).containsExactly("E-1");

		userAccountRepository.deleteByProfessor(professor);
		entityManager.flush();
		assertThat(userAccountRepository.findAll()).isEmpty();
	}

	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}
