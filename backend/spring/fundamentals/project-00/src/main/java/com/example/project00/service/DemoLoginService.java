package com.example.project00.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Role;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.LectureRepository;
import com.example.project00.repository.NotesRepository;
import com.example.project00.repository.ProfessorRepository;
import com.example.project00.repository.StudentRepository;
import com.example.project00.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates read-only, expiring recruiter logins. Each login gets a random username and password and only sees an
 * isolated demo dataset, which is created on first use. Expired logins are removed whenever logins are listed or
 * created.
 */
@Service
@Transactional
public class DemoLoginService {

	/** The kind of account a recruiter explores. */
	public enum Persona {

		STUDENT, PROFESSOR, ADMIN

	}

	/** A generated login without its password. */
	public record DemoLogin(String username, Persona persona, String label, String createdBy, Instant createdAt,
			Instant expiresAt) {
	}

	/** A freshly generated login; the password is only available here and is never stored in plain text. */
	public record CreatedDemoLogin(DemoLogin login, String password) {
	}

	public static final int DEFAULT_DAYS = 7;

	public static final int MAX_DAYS = 30;

	public static final int MAX_LABEL_LENGTH = 100;

	private static final String USERNAME_ALPHABET = "abcdefghijkmnpqrstuvwxyz23456789";

	private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

	private static final int PASSWORD_LENGTH = 18;

	private static final SecureRandom RANDOM = new SecureRandom();

	private record SeedCourse(String code, String title, int credits, List<String> topics) {
	}

	private static final List<SeedCourse> SEED_COURSES = List.of(
			new SeedCourse("DEMO-CS101", "Introduction to Programming", 6,
					List.of("Variables and types", "Control flow", "Functions", "Collections")),
			new SeedCourse("DEMO-CS201", "Algorithms and Data Structures", 6,
					List.of("Complexity", "Sorting", "Graphs", "Dynamic programming")),
			new SeedCourse("DEMO-MA101", "Calculus", 5, List.of("Limits", "Derivatives", "Integrals", "Series")),
			new SeedCourse("DEMO-PH101", "Classical Mechanics", 5,
					List.of("Kinematics", "Newton's laws", "Energy", "Oscillations")));

	private final UserAccountRepository userAccountRepository;

	private final StudentRepository studentRepository;

	private final ProfessorRepository professorRepository;

	private final CourseRepository courseRepository;

	private final LectureRepository lectureRepository;

	private final NotesRepository notesRepository;

	private final PasswordEncoder passwordEncoder;

	private final Clock clock;

	private final int maxActive;

	public DemoLoginService(UserAccountRepository userAccountRepository, StudentRepository studentRepository,
			ProfessorRepository professorRepository, CourseRepository courseRepository,
			LectureRepository lectureRepository, NotesRepository notesRepository, PasswordEncoder passwordEncoder,
			Clock clock, @Value("${app.demo-logins.max-active:25}") int maxActive) {
		this.userAccountRepository = userAccountRepository;
		this.studentRepository = studentRepository;
		this.professorRepository = professorRepository;
		this.courseRepository = courseRepository;
		this.lectureRepository = lectureRepository;
		this.notesRepository = notesRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
		this.maxActive = maxActive;
	}

	/**
	 * Generates a demo login.
	 * @param persona the kind of account
	 * @param label optional note identifying the recruiter (at most {@value #MAX_LABEL_LENGTH} characters)
	 * @param days validity in days, {@code null} for {@value #DEFAULT_DAYS}; between 1 and {@value #MAX_DAYS}
	 * @param createdBy username of the admin or manager generating the login
	 * @return the login together with its one-time visible password
	 * @throws IllegalArgumentException if the persona is missing or the days or label are out of range
	 * @throws IllegalStateException if the maximum number of active demo logins is reached
	 */
	public synchronized CreatedDemoLogin create(Persona persona, String label, Integer days, String createdBy) {
		if (persona == null) {
			throw new IllegalArgumentException("A persona is required");
		}
		int validDays = days == null ? DEFAULT_DAYS : days;
		if (validDays < 1 || validDays > MAX_DAYS) {
			throw new IllegalArgumentException("A demo login must be valid for 1 to " + MAX_DAYS + " days");
		}
		String cleanLabel = label == null || label.isBlank() ? null : label.strip();
		if (cleanLabel != null && cleanLabel.length() > MAX_LABEL_LENGTH) {
			throw new IllegalArgumentException("The label must have at most " + MAX_LABEL_LENGTH + " characters");
		}
		purgeExpired();
		Instant now = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
		if (userAccountRepository.countByDemoExpiresAtAfter(now) >= maxActive) {
			throw new IllegalStateException("At most " + maxActive + " demo logins can be active; revoke one first");
		}
		ensureDataset();
		String username = uniqueUsername();
		String password = randomString(PASSWORD_ALPHABET, PASSWORD_LENGTH);
		String passwordHash = passwordEncoder.encode(password);
		UserAccount account = switch (persona) {
			case STUDENT -> UserAccount.forStudent(createStudentPersona(username), passwordHash);
			case PROFESSOR -> UserAccount.forProfessor(createProfessorPersona(username), passwordHash);
			case ADMIN -> UserAccount.forAdmin(username, passwordHash);
		};
		account.asDemo("Recruiter demo (" + displayRole(persona) + ")", cleanLabel, createdBy, now,
				now.plus(Duration.ofDays(validDays)));
		UserAccount saved = userAccountRepository.save(account);
		return new CreatedDemoLogin(toDemoLogin(saved), password);
	}

	/** @return all demo logins that have not expired, newest first */
	public List<DemoLogin> list() {
		purgeExpired();
		return userAccountRepository.findByDemoExpiresAtIsNotNullOrderByDemoCreatedAtDesc()
			.stream()
			.map(DemoLoginService::toDemoLogin)
			.toList();
	}

	/**
	 * Deletes a demo login and its persona immediately.
	 * @throws ResourceNotFoundException if no demo login has that username
	 */
	public void revoke(String username) {
		UserAccount account = userAccountRepository.findByUsername(username)
			.filter(UserAccount::isDemo)
			.orElseThrow(() -> new ResourceNotFoundException("Demo login", username));
		delete(account);
	}

	/** Removes all expired demo logins. */
	public void purgeExpired() {
		userAccountRepository.findByDemoExpiresAtLessThanEqual(clock.instant()).forEach(this::delete);
	}

	private void delete(UserAccount account) {
		Student student = account.getStudent();
		Professor professor = account.getProfessor();
		userAccountRepository.delete(account);
		if (student != null) {
			notesRepository.deleteAll(notesRepository.findByStudent(student));
			List.copyOf(student.getAttendedLectures()).forEach(lecture -> lecture.removeAttendee(student));
			List.copyOf(student.getEnrolledCourses()).forEach(course -> course.unenroll(student));
			studentRepository.delete(student);
		}
		if (professor != null) {
			for (Lecture lecture : List.copyOf(professor.getLectures())) {
				notesRepository.deleteAll(notesRepository.findByLecture(lecture));
				List.copyOf(lecture.getAttendees()).forEach(lecture::removeAttendee);
				lecture.getCourse().removeLecture(lecture);
				lectureRepository.delete(lecture);
			}
			List.copyOf(professor.getTaughtCourses()).forEach(course -> course.unassignTeacher(professor));
			professorRepository.delete(professor);
		}
	}

	private Student createStudentPersona(String username) {
		Student student = studentRepository.save(new Student(username, "Demo Student", Year.now(clock).getValue())
			.asDemo());
		List<Course> courses = courseRepository.findByDemoTrueOrderByCourseCode();
		LocalDate today = LocalDate.now(clock);
		boolean wroteNotes = false;
		for (Course course : courses.subList(0, Math.min(3, courses.size()))) {
			course.enroll(student);
			for (Lecture lecture : course.getLectures()) {
				if (lecture.getDate().isBefore(today)) {
					lecture.addAttendee(student);
					if (!wroteNotes) {
						notesRepository.save(new Notes(student, lecture,
								"My summary of \"" + lecture.getTopic() + "\": key ideas and open questions.", true));
						wroteNotes = true;
					}
				}
			}
		}
		return student;
	}

	private Professor createProfessorPersona(String username) {
		Professor professor = professorRepository.save(new Professor(username, "Demo Professor", "Computer Science")
			.asDemo());
		List<Course> courses = courseRepository.findByDemoTrueOrderByCourseCode();
		LocalDate today = LocalDate.now(clock);
		int index = 0;
		for (Course course : courses.subList(0, Math.min(2, courses.size()))) {
			course.assignTeacher(professor);
			lectureRepository.save(new Lecture(course, professor, today.plusDays(5L + index * 2L),
					LocalTime.of(14, 0), "Guest lecture: Q&A session", "Demo Hall " + (index + 1)));
			index++;
		}
		return professor;
	}

	/** Creates the shared demo dataset once; it is only visible to demo logins and real admins. */
	private void ensureDataset() {
		if (courseRepository.existsByDemoTrue()) {
			return;
		}
		List<Professor> professors = List.of(
				professorRepository.save(new Professor("demo-prof-ada", "Ada Lovelace", "Computer Science").asDemo()),
				professorRepository.save(new Professor("demo-prof-alan", "Alan Turing", "Mathematics").asDemo()),
				professorRepository.save(new Professor("demo-prof-marie", "Marie Curie", "Physics").asDemo()));
		List<String> names = List.of("Anna Schmidt", "Ben Miller", "Carla Rossi", "David Kim", "Emma Wilson",
				"Felix Nguyen");
		List<Student> students = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			students.add(studentRepository.save(new Student("demo-stud-" + (i + 1), names.get(i), 2023 + i % 3)
				.asDemo()));
		}
		LocalDate today = LocalDate.now(clock);
		int[] dayOffsets = { -21, -7, 3, 14 };
		for (int c = 0; c < SEED_COURSES.size(); c++) {
			SeedCourse seed = SEED_COURSES.get(c);
			Course course = courseRepository.save(new Course(seed.code(), seed.title(), seed.credits()).asDemo());
			Professor teacher = professors.get(c % professors.size());
			course.assignTeacher(teacher);
			List<Student> enrolled = List.of(students.get(c % students.size()),
					students.get((c + 1) % students.size()), students.get((c + 2) % students.size()));
			enrolled.forEach(course::enroll);
			for (int l = 0; l < seed.topics().size(); l++) {
				Lecture lecture = lectureRepository.save(new Lecture(course, teacher,
						today.plusDays(dayOffsets[l]), LocalTime.of(9 + l, 0), seed.topics().get(l),
						"Room " + (101 + c)));
				if (dayOffsets[l] < 0) {
					enrolled.forEach(lecture::addAttendee);
					Notes reviewed = notesRepository.save(new Notes(enrolled.get(0), lecture,
							"Lecture summary: " + seed.topics().get(l) + ". Worked examples and definitions.", false));
					if (l == 0) {
						reviewed.review(teacher, 85 + c, "Clear and complete summary.");
					}
					notesRepository.save(new Notes(enrolled.get(1), lecture,
							"Personal notes on " + seed.topics().get(l).toLowerCase(Locale.ROOT) + ".", true));
				}
			}
		}
	}

	private String uniqueUsername() {
		for (int attempt = 0; attempt < 10; attempt++) {
			String candidate = DemoIds.PREFIX + randomString(USERNAME_ALPHABET, 8);
			if (!userAccountRepository.existsByUsername(candidate)
					&& studentRepository.findByStudentId(candidate).isEmpty()
					&& professorRepository.findByEmployeeId(candidate).isEmpty()) {
				return candidate;
			}
		}
		throw new IllegalStateException("Could not generate a unique demo username");
	}

	private static String randomString(String alphabet, int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
		}
		return builder.toString();
	}

	private static String displayRole(Persona persona) {
		return switch (persona) {
			case STUDENT -> "Student";
			case PROFESSOR -> "Professor";
			case ADMIN -> "Admin";
		};
	}

	private static DemoLogin toDemoLogin(UserAccount account) {
		Persona persona = account.getRole() == Role.STUDENT ? Persona.STUDENT
				: account.getRole() == Role.PROFESSOR ? Persona.PROFESSOR : Persona.ADMIN;
		return new DemoLogin(account.getUsername(), persona, account.getDemoLabel(), account.getDemoCreatedBy(),
				account.getDemoCreatedAt(), account.getDemoExpiresAt());
	}

}
