package com.example.project00.web;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.example.project00.service.DemoTokenService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@TestPropertySource(properties = { "app.demo.enabled=true", "app.demo.revoked-jtis=revoked-1" })
class DemoApiE2ETest extends EndToEndTestSupport {

	private static final KeyPair KEYS = generate();

	private static final KeyPair OTHER_KEYS = generate();

	@DynamicPropertySource
	static void publicKey(DynamicPropertyRegistry registry) {
		registry.add("app.demo.public-key", () -> "-----BEGIN PUBLIC KEY-----\n"
				+ Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(KEYS.getPublic().getEncoded())
				+ "\n-----END PUBLIC KEY-----");
	}

	@Autowired
	private DemoTokenService demoTokens;

	@BeforeEach
	void resetRateLimit() {
		demoTokens.clearFailedAttempts();
	}

	private static KeyPair generate() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
			generator.initialize(new ECGenParameterSpec("secp256r1"));
			return generator.generateKeyPair();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static String sign(KeyPair keys, String jti, Instant notBefore, Instant expires) {
		try {
			JWTClaimsSet claims = new JWTClaimsSet.Builder().jwtID(jti)
				.notBeforeTime(Date.from(notBefore))
				.expirationTime(Date.from(expires))
				.build();
			SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.ES256), claims);
			jwt.sign(new ECDSASigner((ECPrivateKey) keys.getPrivate()));
			return jwt.serialize();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static String validToken() {
		Instant now = Instant.now();
		return sign(KEYS, "ok-1", now.minusSeconds(60), now.plus(10, ChronoUnit.DAYS));
	}

	private void assertRejected(String token) {
		postJson(anonymous, "/api/demo/redeem", Map.of("token", token, "persona", "STUDENT")).expectStatus()
			.isUnauthorized();
	}

	@SuppressWarnings("unchecked")
	private RestTestClient demoClient(String token, String persona) {
		Map<String, Object> body = postJson(anonymous, "/api/demo/redeem", Map.of("token", token, "persona", persona))
			.expectStatus().isOk().expectBody(Map.class).returnResult().getResponseBody();
		return withToken((String) body.get("accessToken"));
	}

	@Test
	void signedTokenGivesReadOnlyPersonaSession() {
		enrollStudent("stud-anna");
		createCourse("CS101");
		RestTestClient demo = demoClient(validToken(), "STUDENT");

		demo.get().uri("/api/auth/me").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.username").isEqualTo("stud-anna")
			.jsonPath("$.role").isEqualTo("STUDENT");
		demo.get().uri("/api/courses").exchange().expectStatus().isOk();
		postJson(demo, "/api/courses", Map.of("courseCode", "X1", "title", "T", "credits", 3)).expectStatus()
			.isForbidden();
	}

	@Test
	void tokenCanBeRedeemedRepeatedly() {
		enrollStudent("stud-anna");
		String token = validToken();

		demoClient(token, "STUDENT");
		demoClient(" " + token + " ", "STUDENT");
	}

	@Test
	void expiredNotYetValidForgedAndRevokedTokensAreRejected() {
		Instant now = Instant.now();
		assertRejected(sign(KEYS, "old", now.minus(11, ChronoUnit.DAYS), now.minusSeconds(5)));
		assertRejected(sign(KEYS, "future", now.plus(1, ChronoUnit.DAYS), now.plus(11, ChronoUnit.DAYS)));
		assertRejected(sign(OTHER_KEYS, "forged", now.minusSeconds(60), now.plus(10, ChronoUnit.DAYS)));
		assertRejected(sign(KEYS, "revoked-1", now.minusSeconds(60), now.plus(10, ChronoUnit.DAYS)));
	}

	@Test
	void invalidTokenIsRejectedThenRateLimited() {
		for (int i = 0; i < 5; i++) {
			assertRejected("not-a-token-" + i);
		}
		postJson(anonymous, "/api/demo/redeem", Map.of("token", "not-a-token-x", "persona", "STUDENT"))
			.expectStatus().isEqualTo(429);
	}

	@Test
	void statusEndpointAnnouncesTheDemoEnvironment() {
		anonymous.get().uri("/api/demo/status").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.enabled").isEqualTo(true);
	}

	@Test
	void serverHasNoEndpointToMintTokens() {
		postJson(client, "/api/demo-tokens", Map.of()).expectStatus().isNotFound();
	}

}
