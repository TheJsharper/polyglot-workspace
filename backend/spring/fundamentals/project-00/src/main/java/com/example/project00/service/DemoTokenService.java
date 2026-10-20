package com.example.project00.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.project00.domain.Role;
import com.example.project00.security.DemoTokenVerifier;
import com.example.project00.security.SlidingWindowLimiter;
import com.example.project00.security.TokenService;
import com.example.project00.security.TokenService.IssuedToken;
import com.nimbusds.jwt.JWTClaimsSet;

/** Redeems signed recruiter tokens for short-lived, read-only local development sessions. */
@Service
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoTokenService {

	private final DemoTokenVerifier verifier;
	private final TokenService tokenService;
	private final SlidingWindowLimiter failedAttempts;
	private final String studentUsername;
	private final String professorUsername;

	public DemoTokenService(DemoTokenVerifier verifier, TokenService tokenService, Clock clock,
			@Value("${app.demo.max-failed-attempts:5}") int maxFailedAttempts,
			@Value("${app.demo.failed-attempts-window:15m}") Duration failedWindow,
			@Value("${app.demo.student-username:stud-anna}") String studentUsername,
			@Value("${app.demo.professor-username:prof-ada}") String professorUsername) {
		this.verifier = verifier;
		this.tokenService = tokenService;
		this.studentUsername = studentUsername;
		this.professorUsername = professorUsername;
		this.failedAttempts = new SlidingWindowLimiter(maxFailedAttempts, failedWindow, clock);
	}

	public record Redeemed(IssuedToken accessToken, String username, String role, Instant expiresAt) {
	}

	public Redeemed redeem(String token, Role persona, String clientKey) {
		if (persona != Role.STUDENT && persona != Role.PROFESSOR) {
			throw new IllegalArgumentException("persona must be STUDENT or PROFESSOR");
		}
		if (failedAttempts.isBlocked(clientKey)) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Too many invalid attempts. Try again later.");
		}
		JWTClaimsSet claims = verifier.verify(token).orElse(null);
		if (claims == null) {
			failedAttempts.record(clientKey);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The demo token is invalid or has expired.");
		}
		Instant expiresAt = claims.getExpirationTime().toInstant();
		String username = persona == Role.STUDENT ? studentUsername : professorUsername;
		IssuedToken issued = tokenService.issueDemo(username, persona.name(), expiresAt);
		return new Redeemed(issued, username, persona.name(), expiresAt);
	}

	public void clearFailedAttempts() {
		failedAttempts.clear();
	}

}
