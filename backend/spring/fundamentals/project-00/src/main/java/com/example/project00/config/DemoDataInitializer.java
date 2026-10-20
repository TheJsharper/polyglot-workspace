package com.example.project00.config;

import java.util.List;
import java.util.stream.IntStream;

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
 * Seeds local development professors, students and courses. Only active with the {@code dev} profile and when the
 * database has no professors, students or courses yet.
 */
@Component
@Profile("dev")
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
				new Person("prof-marie", "Marie Curie", "Physics", "marie.curie@university.test"),
				new Person("prof-katherine", "Katherine Johnson", "Applied Mathematics", "katherine.johnson@university.test"),
				new Person("prof-grace", "Grace Hopper", "Computer Science", "grace.hopper@university.test"),
				new Person("prof-nikola", "Nikola Tesla", "Electrical Engineering", "nikola.tesla@university.test"),
				new Person("prof-rosalind", "Rosalind Franklin", "Chemistry", "rosalind.franklin@university.test"),
				new Person("prof-carl", "Carl Sagan", "Astronomy", "carl.sagan@university.test"),
				new Person("prof-jane", "Jane Goodall", "Biology", "jane.goodall@university.test"),
				new Person("prof-leonardo", "Leonardo da Vinci", "Art and Design", "leonardo.davinci@university.test"));
		profs.forEach(p -> accounts.registerProfessor(p.id(), p.name(), p.extra(), p.email(), DEMO_PASSWORD));

		List<Person> studs = List.of(
				new Person("stud-anna", "Anna Schmidt", "2023", "anna.schmidt@university.test"),
				new Person("stud-ben", "Ben Miller", "2023", "ben.miller@university.test"),
				new Person("stud-carla", "Carla Rossi", "2024", "carla.rossi@university.test"),
				new Person("stud-david", "David Kim", "2024", "david.kim@university.test"),
				new Person("stud-emma", "Emma Wilson", "2025", "emma.wilson@university.test"),
				new Person("stud-felix", "Felix Nguyen", "2025", "felix.nguyen@university.test"),
				new Person("stud-grace", "Grace Patel", "2023", "grace.patel@university.test"),
				new Person("stud-henry", "Henry Brown", "2024", "henry.brown@university.test"),
				new Person("stud-irene", "Irene Garcia", "2025", "irene.garcia@university.test"),
				new Person("stud-jack", "Jack Thompson", "2023", "jack.thompson@university.test"));
		studs.forEach(s -> accounts.registerStudent(s.id(), s.name(), Integer.parseInt(s.extra()), s.email(),
				DEMO_PASSWORD));

		List<String> courseCodes = List.of("CS101", "CS201", "MA101", "PH101", "BI101", "CH101", "HI101", "EN101",
				"EC101", "ST101");
		List<String> courseTitles = List.of("Introduction to Programming", "Algorithms and Data Structures",
				"Calculus", "Classical Mechanics", "Introduction to Biology", "General Chemistry", "World History",
				"Academic Writing", "Principles of Economics", "Statistics");
		List<Integer> credits = List.of(6, 6, 5, 5, 5, 5, 4, 4, 5, 5);
		IntStream.range(0, courseCodes.size()).forEach(i -> {
			String code = courseCodes.get(i);
			courses.create(code, courseTitles.get(i), credits.get(i));
			courses.assignTeacher(code, profs.get(i).id());
			courses.enroll(code, studs.get(i).id());
			courses.enroll(code, studs.get((i + 1) % studs.size()).id());
		});

		log.info("Local development data created: 10 professors, 10 students and 10 courses");
	}

}
