package com.example.project00.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.project00.web.dto.AuthDtos.LoginRequest;
import com.example.project00.web.dto.AuthDtos.MeResponse;
import com.example.project00.web.dto.AuthDtos.RegisterRequest;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import com.example.project00.web.dto.CourseDtos.CourseResponse;
import com.example.project00.web.dto.CourseDtos.CreateCourseRequest;
import com.example.project00.web.dto.ProfessorDtos.CreateProfessorRequest;
import com.example.project00.web.dto.StudentDtos.UpdateStudentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * Registration, login and the role rules of the API.
 */
class AuthE2ETest extends EndToEndTestSupport {

	@Test
	void studentRegistersThemselvesAndLogsIn() {
		postJson(anonymous, "/api/auth/register", RegisterRequest.student("S-1", "Alan", 2025, PASSWORD))
			.expectStatus()
			.isCreated()
			.expectHeader()
			.location("/api/students/S-1")
			.expectBody()
			.jsonPath("$.username").isEqualTo("S-1")
			.jsonPath("$.role").isEqualTo("STUDENT")
			.jsonPath("$.password").doesNotExist();

		TokenResponse token = postJson(anonymous, "/api/auth/login", new LoginRequest("S-1", PASSWORD))
			.expectStatus()
			.isOk()
			.expectBody(TokenResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(token.tokenType()).isEqualTo("Bearer");
		assertThat(token.expiresIn()).isPositive();

		MeResponse me = withToken(token.accessToken()).get()
			.uri("/api/auth/me")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(MeResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(me).isEqualTo(new MeResponse("S-1", "STUDENT", "Alan", null));
	}

	@Test
	void refreshAndLogoutRequireCsrfAndRotateSecureTenDayCookie() {
		enrollStudent("S-1");
		var login = anonymous.post()
			.uri("/api/auth/login")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(new LoginRequest("S-1", PASSWORD))
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(TokenResponse.class)
			.returnResult();
		String refreshSetCookie = login.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertThat(refreshSetCookie).contains("__Host-university-refresh=", "Secure", "HttpOnly", "SameSite=Lax",
				"Path=/", "Max-Age=864000");
		String refreshCookie = refreshSetCookie.substring(0, refreshSetCookie.indexOf(';'));

		var csrf = anonymous.get()
			.uri("/api/auth/csrf")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(CsrfResponse.class)
			.returnResult();
		String csrfToken = csrf.getResponseBody().token();
		String csrfSetCookie = csrf.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
		String csrfCookie = csrfSetCookie.substring(0, csrfSetCookie.indexOf(';'));

		anonymous.post()
			.uri("/api/auth/refresh")
			.header(HttpHeaders.COOKIE, refreshCookie + "; " + csrfCookie)
			.exchange()
			.expectStatus()
			.isForbidden();

		var refresh = anonymous.post()
			.uri("/api/auth/refresh")
			.header(HttpHeaders.COOKIE, refreshCookie + "; " + csrfCookie)
			.header("X-XSRF-TOKEN", csrfToken)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(TokenResponse.class)
			.returnResult();
		String rotatedCookie = refresh.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertThat(rotatedCookie).contains("__Host-university-refresh=", "Max-Age=864000");
		String rotatedRefreshCookie = rotatedCookie.substring(0, rotatedCookie.indexOf(';'));

		anonymous.post()
			.uri("/api/auth/logout")
			.header(HttpHeaders.COOKIE, rotatedRefreshCookie + "; " + csrfCookie)
			.header("X-XSRF-TOKEN", csrfToken)
			.exchange()
			.expectStatus()
			.isNoContent()
			.expectHeader()
			.value(HttpHeaders.SET_COOKIE, cookie -> assertThat(cookie).contains("Max-Age=0", "Secure", "HttpOnly"));
	}

	@Test
	void studentCanLogInWithEmailAsWellAsId() {
		postJson(anonymous, "/api/auth/register",
				new RegisterRequest(com.example.project00.domain.Role.STUDENT, "455", "Pedro", 2025, null, PASSWORD,
						"Pedro.Salomon@gmail.com"))
			.expectStatus()
			.isCreated();

		for (String login : new String[] { "455", "pedro.salomon@gmail.com", "Pedro.Salomon@gmail.com" }) {
			TokenResponse token = postJson(anonymous, "/api/auth/login", new LoginRequest(login, PASSWORD))
				.expectStatus()
				.isOk()
				.expectBody(TokenResponse.class)
				.returnResult()
				.getResponseBody();
			assertThat(token.accessToken()).isNotBlank();
		}
		postJson(anonymous, "/api/auth/register",
				new RegisterRequest(com.example.project00.domain.Role.STUDENT, "456", "Other", 2025, null, PASSWORD,
						"pedro.salomon@gmail.com"))
			.expectStatus()
			.isEqualTo(409);
	}

	@Test
	void adminIsDefinedByConfiguration() {
		client.get().uri("/api/auth/me").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.username").isEqualTo(adminUsername)
			.jsonPath("$.role").isEqualTo("ADMIN");
	}

	@Test
	void registeringTakenIdOrAdminNameReturnsConflict() {
		enrollStudent("S-1");

		postJson(anonymous, "/api/auth/register", RegisterRequest.student("S-1", "Other", 2025, PASSWORD))
			.expectStatus()
			.isEqualTo(409);
		postJson(anonymous, "/api/auth/register", RegisterRequest.student(adminUsername, "Eve", 2025, PASSWORD))
			.expectStatus()
			.isEqualTo(409);
	}

	@Test
	void registrationValidatesPassword() {
		postJson(anonymous, "/api/auth/register", RegisterRequest.student("S-1", "Alan", 2025, "short"))
			.expectStatus()
			.isBadRequest();
	}

	@Test
	void registrationValidatesRoleSpecificFields() {
		postJson(anonymous, "/api/auth/register", RegisterRequest.professor("E-9", "Grace", " ", PASSWORD))
			.expectStatus()
			.isBadRequest();
		postJson(anonymous, "/api/auth/register", new RegisterRequest(null, "S-9", "Alan", null, null, PASSWORD))
			.expectStatus()
			.isBadRequest();
	}

	@Test
	void professorRegistersPubliclyAndLogsIn() {
		postJson(anonymous, "/api/auth/register", RegisterRequest.professor("E-9", "Grace", "Math", PASSWORD))
			.expectStatus()
			.isCreated()
			.expectHeader()
			.location("/api/professors/E-9")
			.expectBody()
			.jsonPath("$.role").isEqualTo("PROFESSOR");

		as("E-9").get().uri("/api/auth/me").exchange().expectBody().jsonPath("$.role").isEqualTo("PROFESSOR");
	}

	@Test
	void onlyAdminRegistersAdmins() {
		enrollStudent("S-1");
		RegisterRequest request = RegisterRequest.admin("A-2", PASSWORD);

		postJson(anonymous, "/api/auth/register", request).expectStatus().isUnauthorized();
		postJson(as("S-1"), "/api/auth/register", request).expectStatus().isForbidden();
		postJson(client, "/api/auth/register", request).expectStatus()
			.isCreated()
			.expectBody()
			.jsonPath("$.role").isEqualTo("ADMIN");
		postJson(client, "/api/auth/register", request).expectStatus().isEqualTo(409);

		as("A-2").get().uri("/api/auth/me").exchange().expectBody().jsonPath("$.role").isEqualTo("ADMIN");
	}

	private record CsrfResponse(String token) {
	}

	@Test
	void wrongPasswordOrUnknownUserReturnsUnauthorized() {
		enrollStudent("S-1");

		postJson(anonymous, "/api/auth/login", new LoginRequest("S-1", "wrong-password")).expectStatus()
			.isUnauthorized()
			.expectHeader()
			.contentType("application/problem+json")
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Invalid username or password");
		postJson(anonymous, "/api/auth/login", new LoginRequest("ghost", PASSWORD)).expectStatus()
			.isUnauthorized()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Invalid username or password");
	}

	@Test
	void missingOrInvalidTokenReturnsUnauthorized() {
		anonymous.get()
			.uri("/api/courses")
			.exchange()
			.expectStatus()
			.isUnauthorized()
			.expectHeader()
			.valueEquals(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
			.expectBody()
			.jsonPath("$.status").isEqualTo(401);

		withToken("not-a-jwt").get().uri("/api/courses").exchange().expectStatus().isUnauthorized();
		String forged = login(adminUsername, adminPassword) + "x";
		withToken(forged).get().uri("/api/courses").exchange().expectStatus().isUnauthorized();
	}

	@Test
	void publicEndpointsNeedNoToken() {
		anonymous.get().uri("/hello").exchange().expectStatus().isOk();
		anonymous.get().uri("/v3/api-docs").exchange().expectStatus().isOk();
	}

	@Test
	void onlyAdminRegistersProfessors() {
		enrollStudent("S-1");
		hireProfessor("E-1");
		CreateProfessorRequest request = new CreateProfessorRequest("E-2", "Grace", "Math", PASSWORD);

		postJson(anonymous, "/api/professors", request).expectStatus().isUnauthorized();
		postJson(as("S-1"), "/api/professors", request).expectStatus().isForbidden();
		postJson(as("E-1"), "/api/professors", request).expectStatus().isForbidden();
		postJson(client, "/api/professors", request).expectStatus().isCreated();

		as("E-2").get().uri("/api/auth/me").exchange().expectBody().jsonPath("$.role").isEqualTo("PROFESSOR");
	}

	@Test
	void onlyAdminManagesCourseCatalog() {
		hireProfessor("E-1");
		enrollStudent("S-1");
		CreateCourseRequest request = new CreateCourseRequest("CS101", "Programming", 6);

		postJson(as("E-1"), "/api/courses", request).expectStatus().isForbidden();
		postJson(as("S-1"), "/api/courses", request).expectStatus().isForbidden();
		createCourse("CS101");

		as("E-1").delete().uri("/api/courses/CS101").exchange().expectStatus().isForbidden();
		as("E-1").put().uri("/api/courses/CS101/professors/E-1").exchange().expectStatus().isForbidden();
		as("S-1").get().uri("/api/courses/CS101").exchange().expectStatus().isOk();
	}

	@Test
	void studentsEnrollOnlyThemselves() {
		createCourse("CS101");
		enrollStudent("S-1");
		enrollStudent("S-2");

		as("S-2").put().uri("/api/courses/CS101/students/S-1").exchange().expectStatus().isForbidden();
		client.put().uri("/api/courses/CS101/students/S-1").exchange().expectStatus().isForbidden();

		CourseResponse course = enrollInCourse("CS101", "S-1");
		assertThat(course.enrolledStudentIds()).containsExactly("S-1");

		as("S-1").delete()
			.uri("/api/courses/CS101/students/S-1")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.enrolledStudentIds.length()").isEqualTo(0);
	}

	@Test
	void studentCannotLeaveCourseWhileAttendingItsLectures() {
		createCourse("CS101");
		hireProfessor("E-1");
		enrollStudent("S-1");
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");

		as("S-1").delete().uri("/api/courses/CS101/students/S-1").exchange().expectStatus().isEqualTo(409);
	}

	@Test
	void studentProfilesArePrivateToStaffAndOwner() {
		hireProfessor("E-1");
		enrollStudent("S-1");
		enrollStudent("S-2");

		as("S-1").get().uri("/api/students/S-1").exchange().expectStatus().isOk();
		as("S-2").get().uri("/api/students/S-1").exchange().expectStatus().isForbidden();
		as("E-1").get().uri("/api/students/S-1").exchange().expectStatus().isOk();
		as("S-1").get().uri("/api/students").exchange().expectStatus().isForbidden();
		as("E-1").get().uri("/api/students").exchange().expectStatus().isOk();

		putJson(as("S-1"), "/api/students/{id}", new UpdateStudentRequest("Alan T.", 2025), "S-1").expectStatus()
			.isOk();
		putJson(as("S-2"), "/api/students/{id}", new UpdateStudentRequest("Hacked", 2025), "S-1").expectStatus()
			.isForbidden();
	}

}
