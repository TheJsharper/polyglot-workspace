package com.example.project00.web;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.project00.security.TokenService;

@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:role-switch-test;DB_CLOSE_DELAY=-1")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class RoleSwitchE2ETest {

	private static final String SWITCH = "/api/auth/switch-role";

	@Autowired
	private RestTestClient anonymous;

	@Autowired
	private TokenService tokens;

	private RestTestClient as(String username, String role) {
		String token = tokens
			.issue(new UsernamePasswordAuthenticationToken(username, "n/a", AuthorityUtils.createAuthorityList("ROLE_" + role)))
			.value();
		return withToken(token);
	}

	private RestTestClient withToken(String token) {
		return anonymous.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
	}

	private String switchTo(RestTestClient caller, String role) {
		return (String) caller.post().uri(SWITCH).contentType(MediaType.APPLICATION_JSON).body(Map.of("role", role))
			.exchange().expectStatus().isOk().expectBody(Map.class).returnResult().getResponseBody().get("accessToken");
	}

	@Test
	void managerSwitchesToEachPersonaAndBack() {
		RestTestClient manager = as("manager", "MANAGER");
		manager.get().uri("/api/auth/me").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.availableRoles.length()").isEqualTo(4);

		for (String role : List.of("STUDENT", "PROFESSOR", "ADMIN")) {
			RestTestClient persona = withToken(switchTo(manager, role));
			persona.get().uri("/api/auth/me").exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.role").isEqualTo(role)
				.jsonPath("$.username").isEqualTo("manager-" + role.toLowerCase())
				.jsonPath("$.availableRoles.length()").isEqualTo(4);
			RestTestClient back = withToken(switchTo(persona, "MANAGER"));
			back.get().uri("/api/auth/me").exchange().expectBody().jsonPath("$.role").isEqualTo("MANAGER");
		}
	}

	@Test
	void studentPersonaOwnsItsStudentRecord() {
		RestTestClient student = withToken(switchTo(as("manager", "MANAGER"), "STUDENT"));
		student.get().uri("/api/students/manager-student").exchange().expectStatus().isOk();
	}

	@Test
	void otherUsersCannotSwitchRoles() {
		anonymous.post().uri(SWITCH).contentType(MediaType.APPLICATION_JSON).body(Map.of("role", "ADMIN")).exchange()
			.expectStatus().isUnauthorized();
		for (String role : List.of("ADMIN", "PROFESSOR", "STUDENT")) {
			RestTestClient caller = as("someone", role);
			caller.post().uri(SWITCH).contentType(MediaType.APPLICATION_JSON).body(Map.of("role", "MANAGER"))
				.exchange().expectStatus().isForbidden();
			caller.get().uri("/api/auth/me").exchange().expectBody().jsonPath("$.availableRoles.length()")
				.isEqualTo(0);
		}
	}

	@Test
	void rejectsUnknownRoles() {
		as("manager", "MANAGER").post().uri(SWITCH).contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("role", "ROOT")).exchange().expectStatus().isBadRequest();
	}

	@Test
	void personaUsernamesCannotBeRegistered() {
		anonymous.post().uri("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("id", "manager-student", "name", "Mallory", "enrollmentYear", 2024, "password", "Passw0rd!long"))
			.exchange().expectStatus().isEqualTo(409);
	}

}
