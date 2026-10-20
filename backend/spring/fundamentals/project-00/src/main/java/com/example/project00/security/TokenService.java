package com.example.project00.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Issues signed (HS256) access tokens for authenticated users.
 * <p>
 * Claims: {@code iss}, {@code sub} (username), {@code iat}, {@code exp} and {@code roles} (e.g.
 * {@code ["STUDENT"]}).
 */
@Service
public class TokenService {

	/** Name of the claim holding the caller's roles. */
	public static final String ROLES_CLAIM = "roles";

	private static final String ROLE_PREFIX = "ROLE_";

	private final JwtEncoder jwtEncoder;

	private final AppSecurityProperties.Jwt settings;

	private final Clock clock;

	TokenService(JwtEncoder jwtEncoder, AppSecurityProperties properties, Clock clock) {
		this.jwtEncoder = jwtEncoder;
		this.settings = properties.jwt();
		this.clock = clock;
	}

	/**
	 * Issues a token for a successfully authenticated user.
	 * @param authentication the authenticated user
	 * @return the encoded token and its lifetime
	 */
	public IssuedToken issue(Authentication authentication) {
		Instant now = clock.instant();
		List<String> roles = authentication.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.filter(authority -> authority != null && authority.startsWith(ROLE_PREFIX))
			.map(authority -> authority.substring(ROLE_PREFIX.length()))
			.toList();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(settings.issuer())
			.subject(authentication.getName())
			.issuedAt(now)
			.expiresAt(now.plus(settings.ttl()))
			.claim(ROLES_CLAIM, roles)
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(value, settings.ttl().toSeconds());
	}

	/** Name of the claim used to enforce read-only demo sessions. */
	public static final String DEMO_CLAIM = "demo";

	/** Issues a read-only development demo session capped at the signed demo token's expiry. */
	public IssuedToken issueDemo(String username, String role, Instant notAfter) {
		Instant now = clock.instant();
		Instant expiresAt = now.plus(settings.ttl());
		if (notAfter.isBefore(expiresAt)) {
			expiresAt = notAfter;
		}
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(settings.issuer())
			.subject(username)
			.issuedAt(now)
			.expiresAt(expiresAt)
			.claim(ROLES_CLAIM, List.of(role))
			.claim(DEMO_CLAIM, true)
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(value, Duration.between(now, expiresAt).toSeconds());
	}

	/**
	 * An encoded access token.
	 *
	 * @param value the compact JWT
	 * @param expiresInSeconds lifetime in seconds
	 */
	public record IssuedToken(String value, long expiresInSeconds) {
	}

}
