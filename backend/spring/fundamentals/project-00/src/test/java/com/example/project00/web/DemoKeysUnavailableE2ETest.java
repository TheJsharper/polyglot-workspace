package com.example.project00.web;

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

/** The demo-keys microservice is unreachable: callers get a clean 503, never a stack trace or a hang. */
@ActiveProfiles("dev")
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:demo-down-test;DB_CLOSE_DELAY=-1",
		"app.demo-keys.base-url=http://127.0.0.1:1",
		"app.demo-keys.connect-timeout=500ms" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DemoKeysUnavailableE2ETest {

	@Autowired
	private RestTestClient client;

	@Autowired
	private TokenService tokens;

	@Test
	void managerCallsAnswerServiceUnavailable() {
		String token = tokens.issue(new UsernamePasswordAuthenticationToken("manager", "n/a",
				AuthorityUtils.createAuthorityList("ROLE_MANAGER"))).value();
		RestTestClient manager = client.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();

		manager.get().uri("/api/manager/demo-keys").exchange().expectStatus().isEqualTo(503);
		manager.post().uri("/api/manager/demo-keys").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("count", 1)).exchange().expectStatus().isEqualTo(503);
	}

	@Test
	void redeemAnswersServiceUnavailableAndDoesNotCountAsAFailedAttempt() {
		for (int i = 0; i < 8; i++) {
			client.post().uri("/api/demo/redeem").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("token", "whatever", "persona", "STUDENT")).exchange().expectStatus().isEqualTo(503);
		}
	}

}
