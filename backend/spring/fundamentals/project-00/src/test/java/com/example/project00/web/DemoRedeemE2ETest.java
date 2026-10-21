package com.example.project00.web;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.project00.support.StubDemoKeysService;

/** Redeeming demo tokens through the microservice: invalid tokens are rate limited, valid ones start a session. */
@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-redeem-test;DB_CLOSE_DELAY=-1")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DemoRedeemE2ETest {

	private static final StubDemoKeysService STUB = StubDemoKeysService.shared();

	@DynamicPropertySource
	static void service(DynamicPropertyRegistry registry) {
		registry.add("app.demo-keys.base-url", STUB::baseUrl);
	}

	@Autowired
	private RestTestClient client;

	private void redeem(String token, String persona, int status) {
		client.post().uri("/api/demo/redeem").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("token", token, "persona", persona)).exchange().expectStatus().isEqualTo(status);
	}

	@Test
	void invalidTokensAreRejectedThenBlocked() {
		for (int i = 0; i < 5; i++) {
			redeem("bad-token", "STUDENT", 401);
		}
		redeem("bad-token", "STUDENT", 429);
	}

	@Test
	void nonDemoPersonasAreRejected() {
		client.post().uri("/api/demo/redeem").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("token", StubDemoKeysService.VALID_TOKEN, "persona", "ADMIN")).exchange()
			.expectStatus().is4xxClientError();
	}

}
