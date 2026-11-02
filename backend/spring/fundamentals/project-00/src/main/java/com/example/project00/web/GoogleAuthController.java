package com.example.project00.web;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.project00.security.GoogleLoginSuccessHandler;
import com.example.project00.security.GoogleOAuthProperties;
import com.example.project00.service.GoogleLoginService;
import com.example.project00.service.RefreshSessionService;
import com.example.project00.web.dto.AuthDtos.GoogleCompleteResponse;
import com.example.project00.web.dto.AuthDtos.GoogleMobileCompleteResponse;
import com.example.project00.web.dto.AuthDtos.MobileGoogleCompleteRequest;
import com.example.project00.web.dto.AuthDtos.GoogleOnboardingRequest;
import com.example.project00.web.dto.AuthDtos.GoogleSessionResponse;
import com.example.project00.web.dto.AuthDtos.GoogleStatusResponse;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

	public static final String MOBILE_STATE_ATTRIBUTE = GoogleAuthController.class.getName() + ".mobileState";
	public static final String MOBILE_CHALLENGE_ATTRIBUTE = GoogleAuthController.class.getName() + ".mobileChallenge";

	private final GoogleOAuthProperties googleProperties;
	private final GoogleLoginService googleLoginService;

	@GetMapping("/google/status")
	GoogleStatusResponse status() {
		return new GoogleStatusResponse(googleProperties.enabled());
	}

	@GetMapping("/google/start")
	ResponseEntity<Void> startGoogleSignIn(HttpServletRequest request, HttpServletResponse response) {
		if (!googleProperties.enabled()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		// Added on the servlet response: a Set-Cookie header on ResponseEntity would replace Tomcat's session cookie.
		response.addHeader(HttpHeaders.SET_COOKIE, GoogleLoginSuccessHandler
			.mobileStateCookie("", java.time.Duration.ZERO, request.isSecure()).toString());
		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.build();
	}

	@GetMapping("/google/mobile/start")
	ResponseEntity<Void> startMobileGoogleSignIn(@RequestParam String state,
			@RequestParam("code_challenge") String codeChallenge, HttpServletRequest request,
			HttpServletResponse response) {
		if (!googleProperties.enabled()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		if (!state.matches("[A-Fa-f0-9]{64}") || !codeChallenge.matches("[A-Za-z0-9_-]{43}")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mobile sign-in state or PKCE challenge");
		}
		var session = request.getSession(true);
		session.setAttribute(MOBILE_STATE_ATTRIBUTE, state);
		session.setAttribute(MOBILE_CHALLENGE_ATTRIBUTE, codeChallenge);
		response.addHeader(HttpHeaders.SET_COOKIE, GoogleLoginSuccessHandler
			.mobileStateCookie(state, java.time.Duration.ofSeconds(-1), request.isSecure()).toString());
		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.build();
	}

	@PostMapping("/google/complete")
	ResponseEntity<GoogleCompleteResponse> completeGoogleSignIn(@RequestParam String ticket,
			@CookieValue(name = GoogleLoginSuccessHandler.BINDING_COOKIE, required = false) String binding,
			@Valid @RequestBody(required = false) GoogleOnboardingRequest onboarding) {
		GoogleSessionResponse result = googleLoginService.complete(ticket, binding, onboarding);
		if (result.onboardingRequired()) {
			return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-store")
				.body(new GoogleCompleteResponse(null, true));
		}
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshSecret()).toString())
			.header(HttpHeaders.SET_COOKIE,
					GoogleLoginSuccessHandler.bindingCookie("", java.time.Duration.ZERO).toString())
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.body(new GoogleCompleteResponse(result.session(), false));
	}

	@PostMapping("/google/mobile/complete")
	ResponseEntity<GoogleMobileCompleteResponse> completeMobileGoogleSignIn(@RequestParam String ticket,
			@Valid @RequestBody MobileGoogleCompleteRequest request) {
		var result = googleLoginService.completeMobile(ticket, request.codeVerifier(), request.onboarding());
		return ResponseEntity.ok()
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.body(new GoogleMobileCompleteResponse(result.session(), result.refreshSecret(), result.onboardingRequired()));
	}

	@GetMapping("/csrf")
	Map<String, String> csrf(CsrfToken token) {
		return Map.of("token", token.getToken());
	}

	private static ResponseCookie refreshCookie(String secret) {
		return ResponseCookie.from("__Host-university-refresh", secret)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path("/")
			.maxAge(RefreshSessionService.MAX_SESSION_AGE)
			.build();
	}
}
