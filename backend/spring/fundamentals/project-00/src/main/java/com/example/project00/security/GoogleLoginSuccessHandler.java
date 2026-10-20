package com.example.project00.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
		String ticket;
		try {
			ticket = googleLoginService.createTicket(identity);
		}
		catch (RuntimeException ex) {
			LOGGER.error("Google sign-in succeeded but the backend could not issue a one-time login ticket", ex);
			redirectFailure(response);
			return;
		}
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

	private void redirectFailure(HttpServletResponse response) throws IOException {
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.sendRedirect(properties.frontendUrl() + "?error=google_signin_failed");
	}
}
