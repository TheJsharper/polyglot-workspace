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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.project00.security.TokenService;
import com.example.project00.support.StubDemoKeysService;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-key-api-test;DB_CLOSE_DELAY=-1")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DemoKeyApiE2ETest {

	private static final String URI = "/api/manager/demo-keys";
	private static final StubDemoKeysService STUB = StubDemoKeysService.shared();

	@DynamicPropertySource
	static void service(DynamicPropertyRegistry registry) {
		registry.add("app.demo-keys.base-url", STUB::baseUrl);
	}

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

	@SuppressWarnings("unchecked")
	private Map<String, Object> awaitJob(RestTestClient manager, String location) throws InterruptedException {
		for (int i = 0; i < 50; i++) {
			Map<String, Object> job = manager.get().uri(location).exchange().expectStatus().isOk()
				.expectBody(Map.class).returnResult().getResponseBody();
			if ("DONE".equals(job.get("status"))) {
				return job;
			}
			Thread.sleep(20);
		}
		throw new AssertionError("job did not finish");
	}

	@Test
	void onlyManagersCanUseTheApi() {
		anonymous.get().uri(URI).exchange().expectStatus().isUnauthorized();
		for (String role : List.of("ADMIN", "PROFESSOR", "STUDENT")) {
			RestTestClient caller = as("someone", role);
			caller.get().uri(URI).exchange().expectStatus().isForbidden();
			caller.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(1)).exchange()
				.expectStatus().isForbidden();
			caller.get().uri(URI + "/jobs/00000000-0000-0000-0000-000000000000").exchange().expectStatus().isForbidden();
			caller.delete().uri(URI + "/1").exchange().expectStatus().isForbidden();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void creationIsAsynchronousAndRelaysTheManagerIdentity() throws Exception {
		RestTestClient manager = as("manager@example.com", "MANAGER");

		String location = manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(2)).exchange()
			.expectStatus().isAccepted()
			.expectHeader().exists(HttpHeaders.LOCATION)
			.expectBody()
			.jsonPath("$.status").isEqualTo("PENDING")
			.returnResult()
			.getResponseHeaders()
			.getFirst(HttpHeaders.LOCATION);
		assertThat(location).startsWith(URI + "/jobs/");

		Map<String, Object> job = awaitJob(manager, location);
		List<Map<String, Object>> keys = (List<Map<String, Object>>) job.get("keys");
		assertThat(keys).hasSize(2).allSatisfy(key -> {
			assertThat(key.get("createdBy")).isEqualTo("manager@example.com");
			assertThat(key).doesNotContainKey("privateKey");
		});
		assertThat(STUB.managers).contains("manager@example.com");
	}

	@Test
	@SuppressWarnings("unchecked")
	void managerListsAndDeletesUnusedKeysButNotRedeemedOnes() throws Exception {
		RestTestClient manager = as("manager", "MANAGER");
		String location = manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(1)).exchange()
			.expectStatus().isAccepted().returnResult(String.class).getResponseHeaders().getFirst(HttpHeaders.LOCATION);
		Map<String, Object> key = ((List<Map<String, Object>>) awaitJob(manager, location).get("keys")).get(0);
		int id = ((Number) key.get("id")).intValue();

		manager.get().uri(URI).exchange().expectStatus().isOk().expectBody().jsonPath("$[0].token").isNotEmpty();
		manager.delete().uri(URI + "/" + id).exchange().expectStatus().isNoContent();
		manager.delete().uri(URI + "/" + id).exchange().expectStatus().isNotFound();
		manager.delete().uri(URI + "/" + STUB.redeemedKeyId()).exchange().expectStatus().isEqualTo(409);
	}

	@Test
	void rejectsInvalidRequestsBeforeCallingTheService() {
		RestTestClient manager = as("manager", "MANAGER");
		for (int count : new int[] { 0, 11, -1 }) {
			manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON).body(body(count)).exchange()
				.expectStatus().isBadRequest();
		}
		manager.post().uri(URI).contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("count", 1, "days", 31)).exchange().expectStatus().isBadRequest();
		manager.get().uri(URI + "/jobs/../../etc").exchange().expectStatus().is4xxClientError();
		manager.get().uri(URI + "/jobs/not-a-job-id").exchange().expectStatus().isBadRequest();
	}

	@Test
	void unknownJobIsNotFound() {
		as("manager", "MANAGER").get().uri(URI + "/jobs/00000000-0000-0000-0000-000000000000").exchange()
			.expectStatus().isNotFound();
	}

}
