package com.example.project00.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import com.example.project00.domain.RefreshCredential;
import com.example.project00.repository.RefreshCredentialRepository;
import com.example.project00.security.TokenService;
import com.example.project00.security.TokenService.IssuedToken;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshSessionService {

	public static final Duration MAX_SESSION_AGE = Duration.ofDays(10);

	private static final SecureRandom RANDOM = new SecureRandom();

	private final RefreshCredentialRepository credentials;
	private final UserDetailsService users;
	private final TokenService tokens;
	private final Clock clock;

	public RefreshSessionService(RefreshCredentialRepository credentials, UserDetailsService users, TokenService tokens,
			Clock clock) {
		this.credentials = credentials;
		this.users = users;
		this.tokens = tokens;
		this.clock = clock;
	}

	@Transactional
	public String issue(String username) {
		Instant now = clock.instant();
		UUID familyId = UUID.randomUUID();
		String secret = randomSecret();
		credentials.save(new RefreshCredential(hash(secret), familyId, username, now.plus(MAX_SESSION_AGE)));
		return secret;
	}

	public TokenResponse issueAccessToken(org.springframework.security.core.Authentication authentication) {
		IssuedToken issued = tokens.issue(authentication);
		return new TokenResponse(issued.value(), "Bearer", issued.expiresInSeconds());
	}

	@Transactional
	public RefreshedSession refresh(String secret) {
		Instant now = clock.instant();
		var current = credentials.findByTokenHashForUpdate(hash(secret));
		if (current.isEmpty()) {
			return null;
		}
		RefreshCredential credential = current.get();
		if (!credential.activeAt(now)) {
			credentials.findAllByFamilyId(credential.getFamilyId()).forEach(item -> item.revoke(now));
			return null;
		}
		credential.revoke(now);
		try {
			UserDetails user = users.loadUserByUsername(credential.getUsername());
			String nextSecret = randomSecret();
			credentials.save(new RefreshCredential(hash(nextSecret), credential.getFamilyId(), credential.getUsername(),
					credential.getExpiresAt()));
			var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
			IssuedToken accessToken = tokens.issue(authentication);
			return new RefreshedSession(new TokenResponse(accessToken.value(), "Bearer", accessToken.expiresInSeconds()),
					nextSecret);
		}
		catch (UsernameNotFoundException ex) {
			credentials.findAllByFamilyId(credential.getFamilyId()).forEach(item -> item.revoke(now));
			return null;
		}
	}

	@Transactional
	public void revoke(String secret) {
		credentials.findByTokenHashForUpdate(hash(secret)).ifPresent(credential ->
				credentials.findAllByFamilyId(credential.getFamilyId()).forEach(item -> item.revoke(clock.instant())));
	}

	private static String randomSecret() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String hash(String secret) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
			return java.util.HexFormat.of().formatHex(digest);
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}

	public record RefreshedSession(TokenResponse access, String refreshSecret) {
	}
}
