package com.example.project00.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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

	@Autowired
	private ClientRegistrationRepository registrations;

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

	@Test
	void mobileStartMarksBrowserSoExpiredSessionFailureStillReturnsToApp() {
		String state = "a".repeat(64);
		var start = anonymous.get()
			.uri("/api/auth/google/mobile/start?state={state}&code_challenge={challenge}", state, "b".repeat(43))
			.exchange();
		start.expectStatus().isFound();
		var startCookies = start.returnResult(Void.class).getResponseHeaders().get("Set-Cookie");
		assertThat(startCookies).anySatisfy(cookie -> assertThat(cookie).startsWith("JSESSIONID="));
		assertThat(startCookies)
			.anySatisfy(cookie -> assertThat(cookie).startsWith("university-google-mobile-state=" + state)
				.contains("HttpOnly")
				.doesNotContain("Max-Age"));

		// No session cookie: Spring cannot find the authorization request, as after a session timeout.
		var callback = anonymous.get()
			.uri("/login/oauth2/code/google?code=expired&state=unknown")
			.header("Cookie", "university-google-mobile-state=" + state)
			.exchange();
		callback.expectStatus().isFound();
		var headers = callback.returnResult(Void.class).getResponseHeaders();
		assertThat(headers.getLocation().toString())
			.isEqualTo("app://sso/callback?error=google_signin_failed&state=" + state);
		assertThat(headers.get("Set-Cookie")).anySatisfy(cookie -> assertThat(cookie)
			.startsWith("university-google-mobile-state=;")
			.contains("Max-Age=0"));
	}

	@Test
	void webCallbackFailureWithoutMobileMarkerReturnsToFrontend() {
		var callback = anonymous.get().uri("/login/oauth2/code/google?code=expired&state=unknown").exchange();
		callback.expectStatus().isFound();
		assertThat(callback.returnResult(Void.class).getResponseHeaders().getLocation().toString())
			.isEqualTo("http://localhost:4300/sso/callback?error=google_signin_failed");
	}

	@Test
	void googleRegistrationConfiguresOidcSignatureVerificationKeys() {
		assertThat(registrations.findByRegistrationId("google").getProviderDetails().getJwkSetUri())
			.isEqualTo("https://www.googleapis.com/oauth2/v3/certs");
	}
}
