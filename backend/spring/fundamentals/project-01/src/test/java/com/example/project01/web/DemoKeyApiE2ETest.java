package com.example.project01.web;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-key-api-test;DB_CLOSE_DELAY=-1")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DemoKeyApiE2ETest {

	private static final String BASE = "/internal/demo-keys";

	@Autowired
	private RestTestClient anonymous;

	@Value("${app.service-auth.secret}")
	private String secret;

	private String serviceToken(String issuer, String audience, long lifetimeSeconds, String secretToUse)
			throws Exception {
		Instant now = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(issuer)
			.audience(audience)
			.subject("project-00")
			.claim("manager", "pedro.salomon@gmail.com")
			.issueTime(Date.from(now))
			.expirationTime(Date.from(now.plusSeconds(lifetimeSeconds)))
			.build();
		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
		jwt.sign(new MACSigner(secretToUse.getBytes(StandardCharsets.UTF_8)));
		return jwt.serialize();
	}

	private RestTestClient service() throws Exception {
		return withToken(serviceToken("project-00", "demo-keys-service", 60, secret));
	}

	private RestTestClient withToken(String token) {
		return anonymous.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
	}

	private String createJob(RestTestClient client, int count) {
		String location = client.post()
			.uri(BASE + "/jobs")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("count", count, "days", 2, "label", "recruiter"))
			.exchange()
			.expectStatus()
			.isAccepted()
			.expectHeader()
			.exists(HttpHeaders.LOCATION)
			.returnResult(String.class)
			.getResponseHeaders()
			.getFirst(HttpHeaders.LOCATION);
		assertThat(location).startsWith(BASE + "/jobs/");
		return location;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> awaitJob(RestTestClient client, String location) throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			Map<String, Object> job = client.get().uri(location).exchange().expectStatus().isOk()
				.expectBody(Map.class).returnResult().getResponseBody();
			if ("DONE".equals(job.get("status")) || "FAILED".equals(job.get("status"))) {
				return job;
			}
			Thread.sleep(50);
		}
		throw new AssertionError("job did not finish");
	}

	@Test
	void rejectsMissingAndInvalidServiceTokens() throws Exception {
		anonymous.get().uri(BASE).exchange().expectStatus().isUnauthorized();
		withToken("garbage").get().uri(BASE).exchange().expectStatus().isUnauthorized();
		withToken(serviceToken("project-00", "demo-keys-service", 60, "another-secret-another-secret-another-secret"))
			.get().uri(BASE).exchange().expectStatus().isUnauthorized();
		withToken(serviceToken("project-00", "someone-else", 60, secret)).get().uri(BASE).exchange()
			.expectStatus().isUnauthorized();
		withToken(serviceToken("intruder", "demo-keys-service", 60, secret)).get().uri(BASE).exchange()
			.expectStatus().isUnauthorized();
		withToken(serviceToken("project-00", "demo-keys-service", 3600, secret)).get().uri(BASE).exchange()
			.expectStatus().isUnauthorized();
		withToken(serviceToken("project-00", "demo-keys-service", -10, secret)).get().uri(BASE).exchange()
			.expectStatus().isUnauthorized();
	}

	@Test
	void unknownRoutesAreDenied() throws Exception {
		service().get().uri("/actuator/env").exchange().expectStatus().is4xxClientError();
	}

	@Test
	@SuppressWarnings("unchecked")
	void jobCreatesKeysAsynchronouslyWithoutExposingPrivateKeys() throws Exception {
		RestTestClient client = service();

		Map<String, Object> job = awaitJob(client, createJob(client, 2));

		assertThat(job.get("status")).isEqualTo("DONE");
		List<Map<String, Object>> keys = (List<Map<String, Object>>) job.get("keys");
		assertThat(keys).hasSize(2).allSatisfy(key -> {
			assertThat(key.get("createdBy")).isEqualTo("pedro.salomon@gmail.com");
			assertThat(key).doesNotContainKey("privateKey");
			assertThat(key.get("token")).isNotNull();
		});
		client.get().uri(BASE).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$[0].privateKey").doesNotExist();
	}

	@Test
	void rejectsCountsOutsideOneToTenAndUnknownJobs() throws Exception {
		RestTestClient client = service();
		for (int count : new int[] { 0, 11, -1 }) {
			client.post().uri(BASE + "/jobs").contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("count", count)).exchange().expectStatus().isBadRequest();
		}
		client.get().uri(BASE + "/jobs/unknown").exchange().expectStatus().isNotFound();
	}

	@Test
	@SuppressWarnings("unchecked")
	void redeemedKeyCannotBeDeletedButUnusedCanBe() throws Exception {
		RestTestClient client = service();
		Map<String, Object> job = awaitJob(client, createJob(client, 2));
		List<Map<String, Object>> keys = (List<Map<String, Object>>) job.get("keys");
		Map<String, Object> used = keys.get(0);
		Map<String, Object> unused = keys.get(1);

		client.post().uri(BASE + "/redeem").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("token", used.get("token"))).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.valid").isEqualTo(true)
			.jsonPath("$.jti").isNotEmpty();

		client.delete().uri(BASE + "/" + ((Number) used.get("id")).intValue()).exchange()
			.expectStatus().isEqualTo(409);
		client.delete().uri(BASE + "/" + ((Number) unused.get("id")).intValue()).exchange()
			.expectStatus().isNoContent();
		client.delete().uri(BASE + "/" + ((Number) unused.get("id")).intValue()).exchange()
			.expectStatus().isNotFound();
		client.post().uri(BASE + "/redeem").contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("token", unused.get("token"))).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.valid").isEqualTo(false);
	}

}
