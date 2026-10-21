package com.example.project00.service;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.domain.Role;
import java.util.Locale;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.MessageDigest;
import java.util.HexFormat;
import com.example.project00.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for creating login accounts. A person and their account are always created in the same
 * transaction, so a failed registration leaves nothing behind.
 */
@Service
@Transactional
public class AccountService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserAccountRepository userAccountRepository;

	private final StudentService studentService;

	private final ProfessorService professorService;

	private final PasswordEncoder passwordEncoder;

	private final String adminUsername;

	/**
	 * @param adminUsername the configured admin login name, which nobody else may use
	 */
	public AccountService(UserAccountRepository userAccountRepository, StudentService studentService,
			ProfessorService professorService, PasswordEncoder passwordEncoder,
			@Value("${app.security.admin.username}") String adminUsername) {
		this.userAccountRepository = userAccountRepository;
		this.studentService = studentService;
		this.professorService = professorService;
		this.passwordEncoder = passwordEncoder;
		this.adminUsername = adminUsername;
	}

	/**
	 * Self-registration: enrolls a student and creates their account (username = student id).
	 * @return the new student
	 * @throws DuplicateResourceException if the student id or username is already taken
	 */
	public Student registerStudent(String studentId, String name, int enrollmentYear, String rawPassword) {
		return registerStudent(studentId, name, enrollmentYear, null, rawPassword);
	}

	/**
	 * Self-registration with an optional email that can also be used to log in.
	 * @throws DuplicateResourceException if the student id, username or email is already taken
	 */
	public Student registerStudent(String studentId, String name, int enrollmentYear, String email,
			String rawPassword) {
		requireFreeUsername(studentId);
		String normalizedEmail = normalizeEmail(email);
		requireFreeEmail(normalizedEmail);
		Student student = studentService.enroll(studentId, name, enrollmentYear);
		userAccountRepository
			.save(UserAccount.forStudent(student, passwordEncoder.encode(rawPassword)).withEmail(normalizedEmail));
		return student;
	}

	/**
	 * Hires a professor and creates their account (username = employee id).
	 * @return the new professor
	 * @throws DuplicateResourceException if the employee id or username is already taken
	 */
	public Professor registerProfessor(String employeeId, String name, String department, String rawPassword) {
		return registerProfessor(employeeId, name, department, null, rawPassword);
	}

	/**
	 * Hires a professor with an optional email that can also be used to log in.
	 * @throws DuplicateResourceException if the employee id, username or email is already taken
	 */
	public Professor registerProfessor(String employeeId, String name, String department, String email,
			String rawPassword) {
		requireFreeUsername(employeeId);
		String normalizedEmail = normalizeEmail(email);
		requireFreeEmail(normalizedEmail);
		Professor professor = professorService.hire(employeeId, name, department);
		userAccountRepository.save(
				UserAccount.forProfessor(professor, passwordEncoder.encode(rawPassword)).withEmail(normalizedEmail));
		return professor;
	}

	/**
	 * Creates an additional admin login (no person attached).
	 * @throws DuplicateResourceException if the username is already taken
	 */
	public UserAccount registerAdmin(String username, String rawPassword) {
		requireFreeUsername(username);
		return userAccountRepository.save(UserAccount.forAdmin(username, passwordEncoder.encode(rawPassword)));
	}

	public UserAccount registerGoogleAccount(String googleSubject, String role, String name, String email,
			int enrollmentYear, String department) {
		var linkedAccount = userAccountRepository.findByGoogleSubject(googleSubject);
		if (linkedAccount.isPresent()) {
			return linkedAccount.get();
		}
		String normalizedEmail = normalizeEmail(email);
		requireFreeEmail(normalizedEmail);
		String username = googleUsername(googleSubject);
		requireFreeUsername(username);
		byte[] randomPassword = new byte[32];
		RANDOM.nextBytes(randomPassword);
		String passwordHash = passwordEncoder.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(randomPassword));
		UserAccount account = switch (Role.valueOf(role)) {
			case STUDENT -> UserAccount.forStudent(studentService.enroll(username, name, enrollmentYear), passwordHash);
			case PROFESSOR -> UserAccount.forProfessor(professorService.hire(username, name, department), passwordHash);
			case ADMIN -> throw new IllegalArgumentException("Google self-registration cannot create administrators");
		};
		return userAccountRepository.save(account.withEmail(normalizedEmail).withGoogleSubject(googleSubject));
	}

	private static String googleUsername(String googleSubject) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(googleSubject.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			return "g-" + HexFormat.of().formatHex(digest);
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}

	private static String normalizeEmail(String email) {
		return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	/**
	 * Display data for the signed-in account.
	 * @param displayName the friendly name to show (never {@code null})
	 * @param pictureUrl a validated Google avatar URL, or {@code null}
	 */
	public record AccountProfile(String displayName, String pictureUrl) {
	}

	/**
	 * Resolves what the UI should show for a username. Falls back to the username itself for the
	 * configured admin, which has no stored account.
	 * @param username the authenticated username
	 * @return the display profile
	 */
	@Transactional(readOnly = true)
	public AccountProfile profileOf(String username) {
		return userAccountRepository.findByUsername(username)
			.map(account -> new AccountProfile(account.resolveDisplayName(), account.getPictureUrl()))
			.orElseGet(() -> new AccountProfile(username, null));
	}

	private void requireFreeEmail(String email) {
		if (email != null && (email.equalsIgnoreCase(adminUsername) || userAccountRepository.existsByEmail(email)
				|| userAccountRepository.existsByUsername(email))) {
			throw new DuplicateResourceException("Account", email);
		}
	}

	private void requireFreeUsername(String username) {
		if (username.equalsIgnoreCase(adminUsername) || userAccountRepository.existsByUsername(username)
				|| (username.contains("@") && userAccountRepository.existsByEmail(username.toLowerCase(Locale.ROOT)))) {
			throw new DuplicateResourceException("Account", username);
		}
	}

}
