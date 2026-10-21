package com.example.project00.web;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.example.project00.security.TokenService;

@ActiveProfiles("prod")
@TestPropertySource(properties = {
		"app.demo.enabled=true",
		"app.google.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:demo-prod-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password="
})
class DemoEndpointsAbsentE2ETest extends EndToEndTestSupport {

	@Autowired
	private JwtEncoder jwtEncoder;

	@Value("${app.security.jwt.issuer}")
	private String issuer;

	@Test
	void recruiterDemoEndpointsAreNotExposed() {
		client.get().uri("/api/demo/status").exchange().expectStatus().isNotFound();
		client.post()
			.uri("/api/demo/redeem")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body("{\"token\":\"unused\",\"persona\":\"STUDENT\"}")
			.exchange()
			.expectStatus()
			.isNotFound();
	}

	@Test
	void managerDemoKeyApiIsNotExposed() {
		client.get().uri("/api/manager/demo-keys").exchange().expectStatus().isNotFound();
	}

	@Test
	void googleSsoIsDisabledUntilProductionCredentialsAreConfigured() {
		client.get().uri("/api/auth/google/status").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.enabled").isEqualTo(false);
		client.get().uri("/api/auth/google/start").exchange().expectStatus().isNotFound();
	}

	@Test
	void legacyReadOnlyDemoTokensAreRejected() {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(issuer)
			.subject("stud-anna")
			.issuedAt(now)
			.expiresAt(now.plusSeconds(600))
			.claim(TokenService.ROLES_CLAIM, List.of("STUDENT"))
			.claim("demo", true)
			.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
			.getTokenValue();

		withToken(token).get().uri("/api/auth/me").exchange().expectStatus().isUnauthorized();
	}

}
