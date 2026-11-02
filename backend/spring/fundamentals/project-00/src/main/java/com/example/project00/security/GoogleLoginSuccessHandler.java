package com.example.project00.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.example.project00.service.GoogleLoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
			redirectFailure(request, response, properties);
			return;
		}
		String binding = GoogleLoginSuccessHandler.newBinding();
		String ticket;
		HttpSession session = request.getSession(false);
		String mobileState = session == null ? null
				: (String) session.getAttribute(com.example.project00.web.GoogleAuthController.MOBILE_STATE_ATTRIBUTE);
		String mobileChallenge = session == null ? null
				: (String) session.getAttribute(com.example.project00.web.GoogleAuthController.MOBILE_CHALLENGE_ATTRIBUTE);
		boolean mobileFlow = mobileState != null && mobileChallenge != null;
		try {
			ticket = mobileFlow ? googleLoginService.createMobileTicket(identity, mobileChallenge)
					: googleLoginService.createTicket(identity, binding);
		}
		catch (RuntimeException ex) {
			LOGGER.error("Google sign-in succeeded but the backend could not issue a one-time login ticket", ex);
			redirectFailure(request, response, properties);
			return;
		}
		String target;
		if (mobileFlow) {
			target = "app://sso/callback?ticket=" + URLEncoder.encode(ticket, StandardCharsets.UTF_8)
					+ "&state=" + URLEncoder.encode(mobileState, StandardCharsets.UTF_8);
		}
		else {
			response.addHeader("Set-Cookie", bindingCookie(binding, Duration.ofMinutes(5)).toString());
			target = properties.frontendUrl() + "?ticket="
					+ URLEncoder.encode(ticket, StandardCharsets.UTF_8);
		}
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.addHeader("Set-Cookie", mobileStateCookie("", Duration.ZERO, request.isSecure()).toString());
		response.sendRedirect(target);
		if (session != null) {
			session.invalidate();
		}
	}

	public static void redirectFailure(HttpServletRequest request, HttpServletResponse response,
			GoogleOAuthProperties properties) throws IOException {
		HttpSession session = request.getSession(false);
		String mobileState = session == null ? null
				: (String) session.getAttribute(com.example.project00.web.GoogleAuthController.MOBILE_STATE_ATTRIBUTE);
		if (mobileState == null) {
			// The session (and the OAuth authorization request) may have expired while the user sat on Google's
			// page; the cookie still tells us the flow came from the native app so it gets a usable error.
			mobileState = mobileStateFromCookie(request);
		}
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Referrer-Policy", "no-referrer");
		response.addHeader("Set-Cookie", mobileStateCookie("", Duration.ZERO, request.isSecure()).toString());
		if (mobileState != null && mobileState.matches("[A-Fa-f0-9]{64}")) {
			response.sendRedirect("app://sso/callback?error=google_signin_failed&state="
					+ URLEncoder.encode(mobileState, StandardCharsets.UTF_8));
		}
		else {
			response.sendRedirect(properties.frontendUrl() + "?error=google_signin_failed");
		}
		if (session != null) {
			session.invalidate();
		}
	}

	/** Cookie that ties a login ticket to the browser that finished the Google flow (login-CSRF defense). */
	public static final String BINDING_COOKIE = "__Host-university-google-binding";

	/** Browser-session cookie marking a Google flow started by the native app (holds the app's state value). */
	public static final String MOBILE_STATE_COOKIE = "university-google-mobile-state";

	public static org.springframework.http.ResponseCookie mobileStateCookie(String value, Duration maxAge,
			boolean secure) {
		return org.springframework.http.ResponseCookie.from(MOBILE_STATE_COOKIE, value)
			.httpOnly(true)
			.secure(secure)
			.sameSite("Lax")
			.path("/")
			.maxAge(maxAge)
			.build();
	}

	private static String mobileStateFromCookie(HttpServletRequest request) {
		jakarta.servlet.http.Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (jakarta.servlet.http.Cookie cookie : cookies) {
			if (MOBILE_STATE_COOKIE.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

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
}
