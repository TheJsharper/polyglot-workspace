package com.example.project00.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.project00.web.dto.AuthDtos.LoginRequest;
import com.example.project00.web.dto.AuthDtos.MeResponse;
import com.example.project00.web.dto.CourseDtos.CourseResponse;
import com.example.project00.web.dto.CourseDtos.CreateCourseRequest;
import com.example.project00.web.dto.StudentDtos.StudentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Generated, read-only recruiter demo logins.
 */
class DemoLoginE2ETest extends EndToEndTestSupport {

	private static final String BASE = "/api/admin/demo-logins";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private record Created(String username, String password, String persona, String label, String createdBy,
			Instant createdAt, Instant expiresAt) {
	}

	private Created generate(String persona) {
		return client.post()
			.uri(BASE)
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Map.of("persona", persona, "days", 3, "label", "ACME recruiter"))
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.valueEquals(HttpHeaders.CACHE_CONTROL, "no-store")
			.expectBody(Created.class)
			.returnResult()
			.getResponseBody();
	}

	@Test
	void adminGeneratesStudentLoginThatOnlySeesDemoDataReadOnly() {
		createCourse("CS101");
		enrollStudent("S-1");

		Created created = generate("STUDENT");
		assertThat(created.username()).matches("demo-[a-z0-9]{8}");
		assertThat(created.password()).hasSize(18);
		assertThat(created.createdBy()).isEqualTo(adminUsername);
		assertThat(created.label()).isEqualTo("ACME recruiter");
		assertThat(created.expiresAt()).isAfter(created.createdAt());

		var recruiter = withToken(login(created.username(), created.password()));
		MeResponse me = recruiter.get()
			.uri("/api/auth/me")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(MeResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(me.role()).isEqualTo("STUDENT");
		assertThat(me.demo()).isTrue();
		assertThat(me.demoExpiresAt()).isEqualTo(created.expiresAt());

		List<CourseResponse> courses = recruiter.get()
			.uri("/api/courses")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<CourseResponse>>() {
			})
			.returnResult()
			.getResponseBody();
		assertThat(courses).isNotEmpty().allMatch(course -> course.courseCode().startsWith("DEMO-"));
		recruiter.get().uri("/api/courses/CS101").exchange().expectStatus().isNotFound();
		recruiter.get().uri("/api/students/{id}", created.username()).exchange().expectStatus().isOk();
		recruiter.get().uri("/api/notes?studentId={id}", created.username()).exchange().expectStatus().isOk();

		postJson(recruiter, "/api/courses", new CreateCourseRequest("CS999", "Nope", 3)).expectStatus().isForbidden();
		recruiter.put()
			.uri("/api/courses/DEMO-CS101/students/{id}", created.username())
			.exchange()
			.expectStatus()
			.isForbidden();
		recruiter.get().uri(BASE).exchange().expectStatus().isForbidden();
	}

	@Test
	void realUsersDoNotSeeDemoDataButAdminsDo() {
		createCourse("CS101");
		hireProfessor("P-1");
		generate("PROFESSOR");

		List<CourseResponse> asProfessor = as("P-1").get()
			.uri("/api/courses")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<CourseResponse>>() {
			})
			.returnResult()
			.getResponseBody();
		assertThat(asProfessor).extracting(CourseResponse::courseCode).containsExactly("CS101");
		as("P-1").get().uri("/api/courses/DEMO-CS101").exchange().expectStatus().isNotFound();

		List<CourseResponse> asAdmin = client.get()
			.uri("/api/courses")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<CourseResponse>>() {
			})
			.returnResult()
			.getResponseBody();
		assertThat(asAdmin).extracting(CourseResponse::courseCode).contains("CS101", "DEMO-CS101");
	}

	@Test
	void demoAdminCannotWriteOrGenerateLogins() {
		enrollStudent("S-1");
		Created created = generate("ADMIN");
		var recruiter = withToken(login(created.username(), created.password()));

		List<StudentResponse> students = recruiter.get()
			.uri("/api/students")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<StudentResponse>>() {
			})
			.returnResult()
			.getResponseBody();
		assertThat(students).isNotEmpty().allMatch(student -> student.studentId().startsWith("demo-"));
		recruiter.get().uri("/api/students/S-1").exchange().expectStatus().isNotFound();
		recruiter.delete().uri("/api/students/S-1").exchange().expectStatus().isForbidden();
		postJson(recruiter, BASE, Map.of("persona", "ADMIN")).expectStatus().isForbidden();
	}

	@Test
	void onlyAdminsGenerateLoginsAndDemoPrefixIsReserved() {
		enrollStudent("S-1");
		postJson(as("S-1"), BASE, Map.of("persona", "STUDENT")).expectStatus().isForbidden();
		as("S-1").get().uri(BASE).exchange().expectStatus().isForbidden();
		postJson(client, "/api/courses", new CreateCourseRequest("DEMO-X1", "Reserved", 3)).expectStatus()
			.isBadRequest();
		postJson(client, BASE, Map.of("persona", "STUDENT", "days", 31)).expectStatus().isBadRequest();
	}

	@Test
	void expiredAndRevokedLoginsStopWorking() {
		Created expiring = generate("STUDENT");
		Created revoked = generate("PROFESSOR");
		login(revoked.username(), revoked.password());

		List<Map<String, Object>> listed = client.get()
			.uri(BASE)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<Map<String, Object>>>() {
			})
			.returnResult()
			.getResponseBody();
		assertThat(listed).extracting(entry -> entry.get("username"))
			.containsExactlyInAnyOrder(expiring.username(), revoked.username());
		assertThat(listed).allMatch(entry -> !entry.containsKey("password"));

		client.delete().uri(BASE + "/{username}", revoked.username()).exchange().expectStatus().isNoContent();
		postJson(anonymous, "/api/auth/login", new LoginRequest(revoked.username(), revoked.password()))
			.expectStatus()
			.isUnauthorized();
		client.get().uri("/api/professors/{id}", revoked.username()).exchange().expectStatus().isNotFound();

		jdbcTemplate.update("update user_accounts set demo_expires_at = ? where username = ?",
				java.sql.Timestamp.from(Instant.now().minusSeconds(60)), expiring.username());
		postJson(anonymous, "/api/auth/login", new LoginRequest(expiring.username(), expiring.password()))
			.expectStatus()
			.isUnauthorized();
		client.get().uri(BASE).exchange().expectStatus().isOk().expectBody().json("[]");
		client.get().uri("/api/students/{id}", expiring.username()).exchange().expectStatus().isNotFound();
		client.delete().uri(BASE + "/{username}", "S-unknown").exchange().expectStatus().isNotFound();
	}

}
