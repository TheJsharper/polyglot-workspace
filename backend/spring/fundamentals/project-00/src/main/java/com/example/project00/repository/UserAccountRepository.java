package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link UserAccount} entities.
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	/**
	 * @param username the login name
	 * @return the account with that username, if any
	 */
	Optional<UserAccount> findByUsername(String username);

	/**
	 * @param username the login name
	 * @return whether an account with that username exists
	 */
	boolean existsByUsername(String username);

	/**
	 * @param email the email address (stored lower-cased)
	 * @return the account with that email, if any
	 */
	Optional<UserAccount> findByEmail(String email);

	/**
	 * @param email the email address (stored lower-cased)
	 * @return whether an account with that email exists
	 */
	boolean existsByEmail(String email);

	/**
	 * Deletes the account of a student, if there is one.
	 * @param student the student
	 */
	void deleteByStudent(Student student);

	/**
	 * Deletes the account of a professor, if there is one.
	 * @param professor the professor
	 */
	void deleteByProfessor(Professor professor);

}
