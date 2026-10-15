package com.example.project00.service;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
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
		requireFreeUsername(studentId);
		Student student = studentService.enroll(studentId, name, enrollmentYear);
		userAccountRepository.save(UserAccount.forStudent(student, passwordEncoder.encode(rawPassword)));
		return student;
	}

	/**
	 * Hires a professor and creates their account (username = employee id).
	 * @return the new professor
	 * @throws DuplicateResourceException if the employee id or username is already taken
	 */
	public Professor registerProfessor(String employeeId, String name, String department, String rawPassword) {
		requireFreeUsername(employeeId);
		Professor professor = professorService.hire(employeeId, name, department);
		userAccountRepository.save(UserAccount.forProfessor(professor, passwordEncoder.encode(rawPassword)));
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

	private void requireFreeUsername(String username) {
		if (username.equalsIgnoreCase(adminUsername) || userAccountRepository.existsByUsername(username)) {
			throw new DuplicateResourceException("Account", username);
		}
	}

}
