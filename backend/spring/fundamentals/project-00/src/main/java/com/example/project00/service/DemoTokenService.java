package com.example.project00.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.project00.domain.Role;
import com.example.project00.security.SlidingWindowLimiter;
import com.example.project00.security.TokenService;
import com.example.project00.security.TokenService.IssuedToken;

/**
 * Redeems signed recruiter tokens for short-lived, read-only local development sessions. Token verification and
 * redemption bookkeeping live in the demo-keys microservice; this service rate-limits failures and issues the session.
 */
@Service
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoTokenService {

	private final DemoKeysClient demoKeys;
	private final TokenService tokenService;
	private final SlidingWindowLimiter failedAttempts;
	private final String studentUsername;
	private final String professorUsername;

	public DemoTokenService(DemoKeysClient demoKeys, TokenService tokenService, Clock clock,
			@Value("${app.demo.max-failed-attempts:5}") int maxFailedAttempts,
			@Value("${app.demo.failed-attempts-window:15m}") Duration failedWindow,
			@Value("${app.demo.student-username:stud-anna}") String studentUsername,
			@Value("${app.demo.professor-username:prof-ada}") String professorUsername) {
		this.demoKeys = demoKeys;
		this.tokenService = tokenService;
		this.studentUsername = studentUsername;
		this.professorUsername = professorUsername;
		this.failedAttempts = new SlidingWindowLimiter(maxFailedAttempts, failedWindow, clock);
	}

	public record Redeemed(IssuedToken accessToken, String username, String role, Instant expiresAt) {
	}

	/**
	 * Verifies the token with the microservice without blocking the request thread.
	 * @return the session, or a future failed with 401 (invalid token) or 5xx (service down)
	 * @throws ResponseStatusException 429 when the caller has too many recent failures
	 */
	public CompletableFuture<Redeemed> redeem(String token, Role persona, String clientKey) {
		if (persona != Role.STUDENT && persona != Role.PROFESSOR) {
			throw new IllegalArgumentException("persona must be STUDENT or PROFESSOR");
		}
		if (failedAttempts.isBlocked(clientKey)) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Too many invalid attempts. Try again later.");
		}
		return demoKeys.redeem(token).thenApply(result -> {
			if (!result.valid()) {
				failedAttempts.record(clientKey);
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"The demo token is invalid or has expired.");
			}
			String username = persona == Role.STUDENT ? studentUsername : professorUsername;
			IssuedToken issued = tokenService.issueDemo(username, persona.name(), result.expiresAt());
			return new Redeemed(issued, username, persona.name(), result.expiresAt());
		});
	}

	public void clearFailedAttempts() {
		failedAttempts.clear();
	}

}
