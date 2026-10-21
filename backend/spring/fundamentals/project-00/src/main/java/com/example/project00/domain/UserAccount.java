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

	@Column(nullable = false, unique = true, length = 254)
	private String username;

	@Column(unique = true, length = 254)
	private String email;

	@Column(name = "google_subject", unique = true, length = 255)
	private String googleSubject;

	@Column(name = "display_name", length = 254)
	private String displayName;

	@Column(name = "picture_url", length = 2048)
	private String pictureUrl;

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
	 * Sets the optional email address that can be used instead of the username to log in.
	 * @param email the address, or {@code null}/blank for none; stored trimmed and lower-cased
	 * @return this account
	 */
	public UserAccount withEmail(String email) {
		this.email = email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
		return this;
	}

	public UserAccount withGoogleSubject(String googleSubject) {
		this.googleSubject = Objects.requireNonNull(googleSubject, "googleSubject");
		return this;
	}

	/**
	 * Stores the display name and avatar URL reported by Google at the latest sign-in.
	 * @param displayName the name, or {@code null}/blank to keep none
	 * @param pictureUrl an already validated avatar URL, or {@code null}
	 * @return this account
	 */
	public UserAccount withGoogleProfile(String displayName, String pictureUrl) {
		this.displayName = displayName == null || displayName.isBlank() ? null : displayName.strip();
		this.pictureUrl = pictureUrl;
		return this;
	}

	/**
	 * @return the name to show for this account: the Google name, the linked person's name, or the username
	 */
	public String resolveDisplayName() {
		if (displayName != null) {
			return displayName;
		}
		if (student != null) {
			return student.getName();
		}
		if (professor != null) {
			return professor.getName();
		}
		return username;
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
