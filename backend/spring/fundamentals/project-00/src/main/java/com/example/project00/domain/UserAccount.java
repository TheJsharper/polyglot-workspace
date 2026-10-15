package com.example.project00.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Login credentials of a {@link Student} or {@link Professor}.
 * <p>
 * The username is the person's business key ({@code studentId} or {@code employeeId}); the password is only
 * stored as a hash. Exactly one of {@link #getStudent()} and {@link #getProfessor()} is set, matching the
 * {@link #getRole() role}.
 */
@Getter
@Entity
@Table(name = "user_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount extends BaseEntity {

	@Column(nullable = false, unique = true, length = 20)
	private String username;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", unique = true)
	private Student student;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professor_id", unique = true)
	private Professor professor;

	private UserAccount(String username, String passwordHash, Role role) {
		this.username = username;
		this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
		this.role = role;
	}

	/**
	 * Creates the account of a student; the username is the student id.
	 * @param student the student
	 * @param passwordHash the encoded password
	 * @return a new, unsaved account
	 * @throws NullPointerException if an argument is {@code null}
	 */
	public static UserAccount forStudent(Student student, String passwordHash) {
		UserAccount account = new UserAccount(Objects.requireNonNull(student, "student").getStudentId(), passwordHash,
				Role.STUDENT);
		account.student = student;
		return account;
	}

	/**
	 * Creates the account of a professor; the username is the employee id.
	 * @param professor the professor
	 * @param passwordHash the encoded password
	 * @return a new, unsaved account
	 * @throws NullPointerException if an argument is {@code null}
	 */
	public static UserAccount forProfessor(Professor professor, String passwordHash) {
		UserAccount account = new UserAccount(Objects.requireNonNull(professor, "professor").getEmployeeId(),
				passwordHash, Role.PROFESSOR);
		account.professor = professor;
		return account;
	}

	/**
	 * Creates an additional admin account that is not linked to a student or professor.
	 * @param username the login name
	 * @param passwordHash the encoded password
	 * @return a new, unsaved account
	 * @throws NullPointerException if an argument is {@code null}
	 */
	public static UserAccount forAdmin(String username, String passwordHash) {
		return new UserAccount(Objects.requireNonNull(username, "username"), passwordHash, Role.ADMIN);
	}

}
