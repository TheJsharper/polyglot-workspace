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

	private static final String MANAGER_USERNAME = "manager";

	private static final String PERSONA_STUDENT = "manager-student";

	private static final String PERSONA_PROFESSOR = "manager-professor";

	private static final String PERSONA_ADMIN = "manager-admin";

	private static final int PERSONA_ENROLLMENT_YEAR = 2024;

	private static final java.util.Map<Role, String> PERSONAS = java.util.Map.of(Role.STUDENT, PERSONA_STUDENT,
			Role.PROFESSOR, PERSONA_PROFESSOR, Role.ADMIN, PERSONA_ADMIN);

	private final UserAccountRepository userAccountRepository;

	private final StudentService studentService;

	private final ProfessorService professorService;

	private final PasswordEncoder passwordEncoder;

	private final String adminUsername;

	private final String managerEmail;

	/**
	 * @param adminUsername the configured admin login name, which nobody else may use
	 * @param managerEmail the only email that may become the demo-key manager (dev only); blank disables it
	 */
	public AccountService(UserAccountRepository userAccountRepository, StudentService studentService,
			ProfessorService professorService, PasswordEncoder passwordEncoder,
			@Value("${app.security.admin.username}") String adminUsername,
			@Value("${app.manager.email:}") String managerEmail) {
		this.userAccountRepository = userAccountRepository;
		this.studentService = studentService;
		this.professorService = professorService;
		this.passwordEncoder = passwordEncoder;
		this.adminUsername = adminUsername;
		this.managerEmail = normalizeEmail(managerEmail);
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

	/**
	 * Creates the single demo-key manager for the configured email, unless a manager already exists. The account
	 * has an unguessable password, so it can only be used through Google sign-in.
	 * @return the created account, or empty when no manager email is configured or a manager already exists
	 * @throws DuplicateResourceException if the configured email already belongs to another account
	 */
	public java.util.Optional<UserAccount> provisionManager() {
		if (managerEmail == null || userAccountRepository.existsByRole(Role.MANAGER)) {
			return java.util.Optional.empty();
		}
		requireFreeEmail(managerEmail);
		if (userAccountRepository.existsByUsername(MANAGER_USERNAME)) {
			throw new DuplicateResourceException("Account", MANAGER_USERNAME);
		}
		return java.util.Optional.of(userAccountRepository
			.save(UserAccount.forManager(MANAGER_USERNAME, randomPasswordHash()).withEmail(managerEmail)));
	}

	/**
	 * Binds a verified Google identity to the manager account the first time it signs in.
	 * @param googleSubject the Google subject
	 * @param email the verified Google email
	 * @return the manager account when the email is the configured manager email, otherwise empty
	 * @throws org.springframework.security.authentication.BadCredentialsException if the manager is bound to a
	 * different Google subject
	 */
	public java.util.Optional<UserAccount> linkManagerGoogleAccount(String googleSubject, String email) {
		String normalized = normalizeEmail(email);
		if (managerEmail == null || !managerEmail.equals(normalized)) {
			return java.util.Optional.empty();
		}
		var manager = userAccountRepository.findByEmailAndRole(normalized, Role.MANAGER);
		if (manager.isPresent() && manager.get().getGoogleSubject() == null) {
			manager.get().withGoogleSubject(googleSubject);
		}
		else if (manager.isPresent() && !googleSubject.equals(manager.get().getGoogleSubject())) {
			throw new org.springframework.security.authentication.BadCredentialsException(
					"The manager is bound to another Google account");
		}
		return manager;
	}

	/**
	 * Creates the dev-only personas (student, professor, admin) the manager can switch to, each with its own
	 * data and an unguessable password. Does nothing unless the manager account exists.
	 * @return {@code true} when at least one persona was created
	 */
	public boolean provisionManagerPersonas() {
		if (!userAccountRepository.existsByRole(Role.MANAGER)) {
			return false;
		}
		boolean created = false;
		if (!userAccountRepository.existsByUsername(PERSONA_STUDENT)) {
			Student student = studentService.enroll(PERSONA_STUDENT, "Pedro (Student)", PERSONA_ENROLLMENT_YEAR);
			userAccountRepository.save(UserAccount.forStudent(student, randomPasswordHash()));
			created = true;
		}
		if (!userAccountRepository.existsByUsername(PERSONA_PROFESSOR)) {
			Professor professor = professorService.hire(PERSONA_PROFESSOR, "Pedro (Professor)", "Computer Science");
			userAccountRepository.save(UserAccount.forProfessor(professor, randomPasswordHash()));
			created = true;
		}
		if (!userAccountRepository.existsByUsername(PERSONA_ADMIN)) {
			userAccountRepository.save(UserAccount.forAdmin(PERSONA_ADMIN, randomPasswordHash()));
			created = true;
		}
		return created;
	}

	/**
	 * The roles the caller may act as: all four for the manager and its personas, none for anyone else.
	 * @param username the authenticated username
	 */
	@Transactional(readOnly = true)
	public java.util.List<String> switchableRoles(String username) {
		if (!MANAGER_USERNAME.equals(username) && !PERSONAS.containsValue(username)) {
			return java.util.List.of();
		}
		if (!userAccountRepository.existsByRole(Role.MANAGER)) {
			return java.util.List.of();
		}
		return java.util.List.of(Role.MANAGER.name(), Role.STUDENT.name(), Role.PROFESSOR.name(),
				Role.ADMIN.name());
	}

	/**
	 * Resolves the account the caller switches to.
	 * @param caller the authenticated username, which must be the manager or one of its personas
	 * @param target the role to act as
	 * @return the username of the target account
	 * @throws org.springframework.security.access.AccessDeniedException if the caller may not switch roles
	 */
	@Transactional(readOnly = true)
	public String switchTarget(String caller, Role target) {
		if (switchableRoles(caller).isEmpty()) {
			throw new org.springframework.security.access.AccessDeniedException("Role switching is not allowed");
		}
		return target == Role.MANAGER ? MANAGER_USERNAME : PERSONAS.get(target);
	}

	private String randomPasswordHash() {
		byte[] randomPassword = new byte[32];
		RANDOM.nextBytes(randomPassword);
		return passwordEncoder.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(randomPassword));
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
			case ADMIN, MANAGER -> throw new IllegalArgumentException(
					"Google self-registration cannot create administrators or managers");
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
	 * @param demoExpiresAt when a generated recruiter demo login expires, or {@code null} for normal accounts
	 */
	public record AccountProfile(String displayName, String pictureUrl, java.time.Instant demoExpiresAt) {

		/** @return whether the account is a generated, read-only recruiter demo login */
		public boolean demo() {
			return demoExpiresAt != null;
		}

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
			.map(account -> new AccountProfile(account.resolveDisplayName(), account.getPictureUrl(),
					account.getDemoExpiresAt()))
			.orElseGet(() -> new AccountProfile(username, null, null));
	}

	private void requireFreeEmail(String email) {
		if (email != null && (email.equalsIgnoreCase(adminUsername) || userAccountRepository.existsByEmail(email)
				|| userAccountRepository.existsByUsername(email))) {
			throw new DuplicateResourceException("Account", email);
		}
	}

	private void requireFreeUsername(String username) {
		DemoIds.requireNotReserved(username);
		if (MANAGER_USERNAME.equalsIgnoreCase(username) || PERSONAS.values().stream().anyMatch(username::equalsIgnoreCase)) {
			throw new DuplicateResourceException("Account", username);
		}
		if (username.equalsIgnoreCase(adminUsername) || userAccountRepository.existsByUsername(username)
				|| (username.contains("@") && userAccountRepository.existsByEmail(username.toLowerCase(Locale.ROOT)))) {
			throw new DuplicateResourceException("Account", username);
		}
	}

}
