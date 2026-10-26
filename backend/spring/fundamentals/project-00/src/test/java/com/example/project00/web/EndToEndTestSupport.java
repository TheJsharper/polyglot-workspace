package com.example.project00.web;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.LectureRepository;
import com.example.project00.repository.NotesRepository;
import com.example.project00.repository.ProfessorRepository;
import com.example.project00.repository.StudentRepository;
import com.example.project00.repository.UserAccountRepository;
import com.example.project00.web.dto.AuthDtos.LoginRequest;
import com.example.project00.web.dto.AuthDtos.RegisterRequest;
import com.example.project00.web.dto.AuthDtos.RegisterResponse;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import com.example.project00.web.dto.CourseDtos.CourseResponse;
import com.example.project00.web.dto.CourseDtos.CreateCourseRequest;
import com.example.project00.web.dto.LectureDtos.LectureResponse;
import com.example.project00.web.dto.LectureDtos.ScheduleLectureRequest;
import com.example.project00.web.dto.NotesDtos.NotesResponse;
import com.example.project00.web.dto.NotesDtos.WriteNotesRequest;
import com.example.project00.web.dto.ProfessorDtos.CreateProfessorRequest;
import com.example.project00.web.dto.ProfessorDtos.ProfessorResponse;
import com.example.project00.web.dto.StudentDtos.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Runs the whole application on a random port and talks to it over real HTTP.
 * <p>
 * {@link #client} is authenticated as the built-in admin; {@link #anonymous} sends no token, and
 * {@link #as(String)} returns a client authenticated as any registered student or professor. Every student
 * and professor created through the helpers gets the password {@link #PASSWORD}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
abstract class EndToEndTestSupport {

	protected static final LocalDate DATE = LocalDate.of(2026, 10, 14);

	protected static final LocalTime TIME = LocalTime.of(9, 0);

	protected static final String PASSWORD = "password-123";

	@Autowired
	protected RestTestClient anonymous;

	/** Client authenticated as the admin. */
	protected RestTestClient client;

	@Value("${app.security.admin.username}")
	protected String adminUsername;

	@Value("${app.security.admin.password}")
	protected String adminPassword;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private NotesRepository notesRepository;

	@Autowired
	private LectureRepository lectureRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private ProfessorRepository professorRepository;

	@Autowired
	private StudentRepository studentRepository;

	private final Map<String, RestTestClient> clients = new HashMap<>();

	// The in-memory database outlives each test, so start every test from an empty schema.
	@BeforeEach
	void resetDatabase() {
		userAccountRepository.deleteAllInBatch();
		notesRepository.deleteAllInBatch();
		lectureRepository.deleteAll();
		// deleteAll (not in batch) so Hibernate also clears the enrollment and teaching join tables
		courseRepository.deleteAll();
		professorRepository.deleteAllInBatch();
		studentRepository.deleteAllInBatch();
		clients.clear();
		client = withToken(login(adminUsername, adminPassword));
	}

	/**
	 * @param username a student id or employee id whose password is {@link #PASSWORD}
	 * @return a client sending that user's bearer token
	 */
	protected RestTestClient as(String username) {
		return clients.computeIfAbsent(username, user -> withToken(login(user, PASSWORD)));
	}

	protected String login(String username, String password) {
		return anonymous.post()
			.uri("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new LoginRequest(username, password))
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(TokenResponse.class)
			.returnResult()
			.getResponseBody()
			.accessToken();
	}

	protected RestTestClient withToken(String token) {
		return anonymous.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
	}

	protected CourseResponse createCourse(String courseCode) {
		return post(client, "/api/courses", new CreateCourseRequest(courseCode, "Course " + courseCode, 6),
				CourseResponse.class);
	}

	/** Registers a professor (as admin) with {@link #PASSWORD}. */
	protected ProfessorResponse hireProfessor(String employeeId) {
		return post(client, "/api/professors",
				new CreateProfessorRequest(employeeId, "Professor " + employeeId, "CS", PASSWORD),
				ProfessorResponse.class);
	}

	/** Self-registers a student account with {@link #PASSWORD}. */
	protected StudentResponse enrollStudent(String studentId) {
		post(anonymous, "/api/auth/register", RegisterRequest.student(studentId, "Student " + studentId, 2025, PASSWORD),
				RegisterResponse.class);
		return client.get()
			.uri("/api/students/{id}", studentId)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(StudentResponse.class)
			.returnResult()
			.getResponseBody();
	}

	/** Admin assigns a professor to teach a course. */
	protected CourseResponse assignTeacher(String courseCode, String employeeId) {
		return client.put()
			.uri("/api/courses/{code}/professors/{employeeId}", courseCode, employeeId)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(CourseResponse.class)
			.returnResult()
			.getResponseBody();
	}

	/** The student registers themselves into a course. */
	protected CourseResponse enrollInCourse(String courseCode, String studentId) {
		return as(studentId).put()
			.uri("/api/courses/{code}/students/{studentId}", courseCode, studentId)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(CourseResponse.class)
			.returnResult()
			.getResponseBody();
	}

	/** Makes the professor a teacher of the course (if needed) and schedules the lecture as that professor. */
	protected LectureResponse scheduleLecture(String courseCode, String employeeId, LocalDate date, LocalTime time,
			String topic) {
		assignTeacher(courseCode, employeeId);
		return post(as(employeeId), "/api/lectures",
				new ScheduleLectureRequest(courseCode, employeeId, date, time, topic, "A-101"),
				LectureResponse.class);
	}

	/** Enrolls the student in the lecture's course (if needed) and records attendance as the lecturer. */
	protected LectureResponse recordAttendance(Long lectureId, String studentId) {
		LectureResponse lecture = client.get()
			.uri("/api/lectures/{id}", lectureId)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(LectureResponse.class)
			.returnResult()
			.getResponseBody();
		enrollInCourse(lecture.courseCode(), studentId);
		return as(lecture.employeeId()).put()
			.uri("/api/lectures/{id}/attendees/{studentId}", lectureId, studentId)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(LectureResponse.class)
			.returnResult()
			.getResponseBody();
	}

	/** Writes public notes as the student. */
	protected NotesResponse writeNotes(String studentId, Long lectureId, String content) {
		return post(client, "/api/notes", new WriteNotesRequest(studentId, lectureId, content, false),
				NotesResponse.class);
	}

	protected <T> T post(RestTestClient caller, String uri, Object body, Class<T> responseType) {
		return caller.post()
			.uri(uri)
			.contentType(MediaType.APPLICATION_JSON)
			.body(body)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectBody(responseType)
			.returnResult()
			.getResponseBody();
	}

	protected RestTestClient.ResponseSpec postJson(String uri, Object body) {
		return postJson(client, uri, body);
	}

	protected RestTestClient.ResponseSpec postJson(RestTestClient caller, String uri, Object body) {
		return caller.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(body).exchange();
	}

	protected RestTestClient.ResponseSpec putJson(String uri, Object body, Object... uriVariables) {
		return putJson(client, uri, body, uriVariables);
	}

	protected RestTestClient.ResponseSpec putJson(RestTestClient caller, String uri, Object body,
			Object... uriVariables) {
		return caller.put().uri(uri, uriVariables).contentType(MediaType.APPLICATION_JSON).body(body).exchange();
	}

}