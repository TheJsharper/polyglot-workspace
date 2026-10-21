package com.example.project00.web;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.project00.security.GoogleOAuthProperties;
import com.example.project00.service.GoogleLoginService;
import com.example.project00.service.RefreshSessionService;
import com.example.project00.web.dto.AuthDtos.GoogleCompleteResponse;
import com.example.project00.web.dto.AuthDtos.GoogleOnboardingRequest;
import com.example.project00.web.dto.AuthDtos.GoogleSessionResponse;
import com.example.project00.web.dto.AuthDtos.GoogleStatusResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class GoogleAuthController {

	private final GoogleOAuthProperties googleProperties;
	private final GoogleLoginService googleLoginService;

	@GetMapping("/google/status")
	GoogleStatusResponse status() {
		return new GoogleStatusResponse(googleProperties.enabled());
	}

	@GetMapping("/google/start")
	ResponseEntity<Void> startGoogleSignIn() {
		if (!googleProperties.enabled()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.build();
	}

	@PostMapping("/google/complete")
	ResponseEntity<GoogleCompleteResponse> completeGoogleSignIn(@RequestParam String ticket,
			@Valid @RequestBody(required = false) GoogleOnboardingRequest onboarding) {
		GoogleSessionResponse result = googleLoginService.complete(ticket, onboarding);
		if (result.onboardingRequired()) {
			return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "no-store")
				.body(new GoogleCompleteResponse(null, true));
		}
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshSecret()).toString())
			.header(HttpHeaders.CACHE_CONTROL, "no-store")
			.body(new GoogleCompleteResponse(result.session(), false));
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
