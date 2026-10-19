package com.example.project00.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.ProfessorRepository;
import com.example.project00.repository.StudentRepository;
import com.example.project00.service.AccountService;
import com.example.project00.service.CourseService;

/**
 * Seeds demo professors, students and courses. Only active with the {@code dev} profile and only when the
 * database has no professors, students or courses yet.
 */
@Component
@Profile({ "dev", "demo" })
public class DemoDataInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

	static final String DEMO_PASSWORD = "Password-123";

	private record Person(String id, String name, String extra, String email) {
	}

	private final AccountService accounts;
	private final CourseService courses;
	private final StudentRepository students;
	private final ProfessorRepository professors;
	private final CourseRepository courseRepository;

	public DemoDataInitializer(AccountService accounts, CourseService courses, StudentRepository students,
			ProfessorRepository professors, CourseRepository courseRepository) {
		this.accounts = accounts;
		this.courses = courses;
		this.students = students;
		this.professors = professors;
		this.courseRepository = courseRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (students.count() > 0 || professors.count() > 0 || courseRepository.count() > 0) {
			log.info("Demo data skipped: the database already contains data");
			return;
		}
		List<Person> profs = List.of(
				new Person("prof-ada", "Ada Lovelace", "Computer Science", "ada.lovelace@university.test"),
				new Person("prof-alan", "Alan Turing", "Mathematics", "alan.turing@university.test"),
				new Person("prof-marie", "Marie Curie", "Physics", "marie.curie@university.test"));
		profs.forEach(p -> accounts.registerProfessor(p.id(), p.name(), p.extra(), p.email(), DEMO_PASSWORD));

		List<Person> studs = List.of(
				new Person("stud-anna", "Anna Schmidt", "2023", "anna.schmidt@university.test"),
				new Person("stud-ben", "Ben Miller", "2023", "ben.miller@university.test"),
				new Person("stud-carla", "Carla Rossi", "2024", "carla.rossi@university.test"),
				new Person("stud-david", "David Kim", "2024", "david.kim@university.test"));
		studs.forEach(s -> accounts.registerStudent(s.id(), s.name(), Integer.parseInt(s.extra()), s.email(),
				DEMO_PASSWORD));

		courses.create("CS101", "Introduction to Programming", 6);
		courses.create("CS201", "Algorithms and Data Structures", 6);
		courses.create("MA101", "Calculus", 5);
		courses.create("PH101", "Classical Mechanics", 5);

		courses.assignTeacher("CS101", "prof-ada");
		courses.assignTeacher("CS201", "prof-ada");
		courses.assignTeacher("MA101", "prof-alan");
		courses.assignTeacher("PH101", "prof-marie");

		courses.enroll("CS101", "stud-anna");
		courses.enroll("CS101", "stud-ben");
		courses.enroll("CS201", "stud-anna");
		courses.enroll("CS201", "stud-carla");
		courses.enroll("MA101", "stud-ben");
		courses.enroll("MA101", "stud-david");
		courses.enroll("PH101", "stud-carla");
		courses.enroll("PH101", "stud-david");

		log.info("Demo data created: 3 professors, 4 students, 4 courses (password for all demo accounts: {})",
				DEMO_PASSWORD);
	}

}
