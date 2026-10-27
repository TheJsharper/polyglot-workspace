package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select account from UserAccount account where account.username = :username")
	Optional<UserAccount> findByUsernameForUpdate(@Param("username") String username);

	Optional<UserAccount> findByGoogleSubject(String googleSubject);

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
	 * @param role the role
	 * @return whether any account has that role
	 */
	boolean existsByRole(com.example.project00.domain.Role role);

	/**
	 * @param email the email address (stored lower-cased)
	 * @param role the role
	 * @return the account with that email and role, if any
	 */
	Optional<UserAccount> findByEmailAndRole(String email, com.example.project00.domain.Role role);

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

	/** @return all generated recruiter demo logins, newest first */
	java.util.List<UserAccount> findByDemoExpiresAtIsNotNullOrderByDemoCreatedAtDesc();

	/**
	 * @param now the current instant
	 * @return the demo logins that have expired
	 */
	java.util.List<UserAccount> findByDemoExpiresAtLessThanEqual(java.time.Instant now);

	/**
	 * @param now the current instant
	 * @return the number of demo logins that are still valid
	 */
	long countByDemoExpiresAtAfter(java.time.Instant now);

}
