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
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-key-api-test;DB_CLOSE_DELAY=-1")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DemoKeyApiE2ETest {

	private static final String URI = "/api/manager/demo-keys";

	@Autowired
	private RestTestClient anonymous;

	@Autowired
	private TokenService tokens;

	private RestTestClient as(String username, String role) {
		String token = tokens
			.issue(new UsernamePasswordAuthenticationToken(username, "n/a", AuthorityUtils.createAuthorityList("ROLE_" + role)))
			.value();
		return anonymous.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
	}

	private static Map<String, Object> body(int count) {
		return Map.of("count", count, "days", 2, "label", "recruiter");
	}

	@Test
	void onlyManagersCanUseTheApi() {
		anonymous.get().uri(URI).exchange().expectStatus().isUnauthorized();
		for (String role : List.of("ADMIN", "PROFESSOR", "STUDENT")) {
			RestTestClient caller = as("someone", role);
			caller.get().uri(URI).exchange().expectStatus().isForbidden();
			caller.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(1)).exchange()
				.expectStatus().isForbidden();
			caller.delete().uri(URI + "/1").exchange().expectStatus().isForbidden();
		}
	}

	@Test
	void managerCreatesListsAndDeletesKeysWithoutExposingPrivateKeys() {
		RestTestClient manager = as("manager", "MANAGER");

		manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(2)).exchange()
			.expectStatus().isCreated()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(2)
			.jsonPath("$[0].createdBy").isEqualTo("manager")
			.jsonPath("$[0].redeemed").isEqualTo(false)
			.jsonPath("$[0].privateKey").doesNotExist();

		manager.get().uri(URI).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$[0].token").isNotEmpty()
			.jsonPath("$[0].privateKey").doesNotExist();

		List<?> all = manager.get().uri(URI).exchange().expectBody(List.class).returnResult().getResponseBody();
		int id = ((Number) ((Map<?, ?>) all.get(0)).get("id")).intValue();
		manager.delete().uri(URI + "/" + id).exchange().expectStatus().isNoContent();
		manager.delete().uri(URI + "/" + id).exchange().expectStatus().isNotFound();
	}

	@Test
	void redeemedKeyCannotBeDeleted() {
		RestTestClient manager = as("manager", "MANAGER");
		List<?> created = manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(1)).exchange()
			.expectStatus().isCreated().expectBody(List.class).returnResult().getResponseBody();
		Map<?, ?> key = (Map<?, ?>) created.get(0);

		anonymous.post().uri("/api/demo/redeem").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("token", key.get("token"), "persona", "STUDENT")).exchange().expectStatus().isOk();

		manager.delete().uri(URI + "/" + ((Number) key.get("id")).intValue()).exchange()
			.expectStatus().isEqualTo(409);
	}

	@Test
	void rejectsCountsOutsideOneToTen() {
		RestTestClient manager = as("manager", "MANAGER");
		for (int count : new int[] { 0, 11, -1 }) {
			manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(count)).exchange()
				.expectStatus().isBadRequest();
		}
		manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("count", 1, "days", 31)).exchange().expectStatus().isBadRequest();
	}

}
