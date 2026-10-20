package com.example.project00.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
		"app.google.enabled=true",
		"app.google.client-id=test-google-client",
		"app.google.client-secret=test-google-secret",
		"app.google.redirect-uri=http://localhost:8080/login/oauth2/code/google",
		"app.google.frontend-url=http://localhost:4300/sso/callback"
})
class GoogleOAuthConfigurationE2ETest extends EndToEndTestSupport {

	@Test
	void enabledGoogleSsoStartsAuthorizationCodeFlow() {
		var result = anonymous.get().uri("/api/auth/google/start").exchange();
		result.expectStatus().isFound();
		assertThat(result.returnResult(Void.class).getResponseHeaders().getLocation().toString())
			.isEqualTo("/oauth2/authorization/google");

		var authorization = anonymous.get().uri("/oauth2/authorization/google").exchange();
		authorization.expectStatus().isFound();
		String location = authorization.returnResult(Void.class).getResponseHeaders().getLocation().toString();
		assertThat(location).startsWith("https://accounts.google.com/");
		assertThat(location).contains("client_id=test-google-client", "response_type=code", "openid");
	}
}
