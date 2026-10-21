package com.example.project00.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;

import com.example.project00.domain.GoogleLoginTicket;
import com.example.project00.domain.Role;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.GoogleLoginTicketRepository;
import com.example.project00.repository.UserAccountRepository;
import com.example.project00.web.dto.AuthDtos.GoogleSessionResponse;
import com.example.project00.web.dto.AuthDtos.GoogleOnboardingRequest;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleLoginService {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final Duration TICKET_LIFETIME = Duration.ofMinutes(5);

	private final GoogleLoginTicketRepository tickets;
	private final UserAccountRepository accounts;
	private final AccountService accountService;
	private final UserDetailsService users;
	private final RefreshSessionService refreshSessions;
	private final Clock clock;

	public GoogleLoginService(GoogleLoginTicketRepository tickets, UserAccountRepository accounts,
			AccountService accountService, UserDetailsService users,
			RefreshSessionService refreshSessions, Clock clock) {
		this.tickets = tickets;
		this.accounts = accounts;
		this.accountService = accountService;
		this.users = users;
		this.refreshSessions = refreshSessions;
		this.clock = clock;
	}

	@Transactional
	public String createTicket(OidcUser identity) {
		String subject = identity.getSubject();
		String email = identity.getEmail();
		if (subject == null || subject.isBlank() || email == null || email.isBlank()
				|| !Boolean.TRUE.equals(identity.getEmailVerified())) {
			throw new BadCredentialsException("Google must provide a verified email address");
		}
		String secret = randomSecret();
		String displayName = identity.getFullName();
		if (displayName == null || displayName.isBlank()) {
			displayName = email.substring(0, email.indexOf('@'));
		}
		displayName = displayName.strip();
		if (displayName.length() > 254) {
			displayName = displayName.substring(0, 254);
		}
		tickets.save(new GoogleLoginTicket(hash(secret), subject, email.toLowerCase(Locale.ROOT), displayName,
				safePictureUrl(identity.getPicture()), clock.instant().plus(TICKET_LIFETIME)));
		return secret;
	}

	/**
	 * Accepts only HTTPS avatar URLs served by Google's user-content CDN so the browser never loads
	 * an arbitrary third-party URL from an identity claim.
	 * @param picture the {@code picture} claim, possibly {@code null}
	 * @return the URL when it is safe to render, otherwise {@code null}
	 */
	static String safePictureUrl(String picture) {
		if (picture == null || picture.isBlank() || picture.length() > 2048) {
			return null;
		}
		try {
			URI uri = new URI(picture);
			String host = uri.getHost();
			if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null || host == null) {
				return null;
			}
			host = host.toLowerCase(Locale.ROOT);
			if (!host.equals("googleusercontent.com") && !host.endsWith(".googleusercontent.com")) {
				return null;
			}
			return uri.toString();
		}
		catch (URISyntaxException ex) {
			return null;
		}
	}

	@Transactional
	public GoogleSessionResponse complete(String secret, GoogleOnboardingRequest onboarding) {
		var ticketOptional = tickets.findByTokenHashForUpdate(hash(secret));
		if (ticketOptional.isEmpty()) {
			throw new BadCredentialsException("Google sign-in expired or was already used");
		}
		GoogleLoginTicket ticket = ticketOptional.get();
		if (ticket.isConsumed() || !ticket.getExpiresAt().isAfter(clock.instant())) {
			throw new BadCredentialsException("Google sign-in expired or was already used");
		}
		UserAccount account = accounts.findByGoogleSubject(ticket.getGoogleSubject()).orElse(null);
		if (account == null) {
			account = accountService.linkManagerGoogleAccount(ticket.getGoogleSubject(), ticket.getEmail())
				.orElse(null);
		}
		boolean created = account == null;
		if (created) {
			if (onboarding == null) {
				return new GoogleSessionResponse(null, null, true, true);
			}
			if (onboarding.role() == null || onboarding.role() == Role.ADMIN || onboarding.role() == Role.MANAGER
					|| !onboarding.isValidForRole()) {
				throw new IllegalArgumentException("Select Student or Professor and complete the required profile fields");
			}
			account = accountService.registerGoogleAccount(ticket.getGoogleSubject(), onboarding.role().name(),
					ticket.getDisplayName(), ticket.getEmail(),
					onboarding.enrollmentYear() == null ? 0 : onboarding.enrollmentYear(), onboarding.department());
		}
		ticket.consume();
		account.withGoogleProfile(ticket.getDisplayName(), ticket.getPictureUrl());
		UserDetails user = users.loadUserByUsername(account.getUsername());
		var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
		TokenResponse issued = refreshSessions.issueAccessToken(authentication);
		String refresh = refreshSessions.issue(account.getUsername());
		return new GoogleSessionResponse(issued, refresh, created, false);
	}

	private static String randomSecret() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String hash(String value) {
		try {
			return java.util.HexFormat.of().formatHex(
					MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}
}
