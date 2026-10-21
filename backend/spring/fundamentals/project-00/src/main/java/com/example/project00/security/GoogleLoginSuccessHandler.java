package com.example.project00.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.example.project00.service.GoogleLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleLoginSuccessHandler.class);

	private final GoogleLoginService googleLoginService;
	private final GoogleOAuthProperties properties;

	public GoogleLoginSuccessHandler(GoogleLoginService googleLoginService, GoogleOAuthProperties properties) {
		this.googleLoginService = googleLoginService;
		this.properties = properties;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if (!(authentication.getPrincipal() instanceof OidcUser identity)) {
			redirectFailure(response);
			return;
		}
		String binding = GoogleLoginSuccessHandler.newBinding();
		String ticket;
		try {
			ticket = googleLoginService.createTicket(identity, binding);
		}
		catch (RuntimeException ex) {
			LOGGER.error("Google sign-in succeeded but the backend could not issue a one-time login ticket", ex);
			redirectFailure(response);
			return;
		}
		response.addHeader("Set-Cookie", bindingCookie(binding, Duration.ofMinutes(5)).toString());
		String target = properties.frontendUrl() + "?ticket="
				+ URLEncoder.encode(ticket, StandardCharsets.UTF_8);
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.sendRedirect(target);
		var session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	/** Cookie that ties a login ticket to the browser that finished the Google flow (login-CSRF defense). */
	public static final String BINDING_COOKIE = "__Host-university-google-binding";

	private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

	static String newBinding() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static org.springframework.http.ResponseCookie bindingCookie(String value, java.time.Duration maxAge) {
		return org.springframework.http.ResponseCookie.from(BINDING_COOKIE, value)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path("/")
			.maxAge(maxAge)
			.build();
	}

	private void redirectFailure(HttpServletResponse response) throws IOException {
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.sendRedirect(properties.frontendUrl() + "?error=google_signin_failed");
	}
}
