package com.example.project01.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates calls from trusted services: an HS256 bearer token signed with the shared secret, issued by the
 * expected service, addressed to this service and valid for at most {@link #MAX_LIFETIME}. The acting manager is
 * carried in the {@code manager} claim and becomes the authentication name.
 */
class ServiceTokenFilter extends OncePerRequestFilter {

	static final Duration MAX_LIFETIME = Duration.ofMinutes(5);
	private static final String PREFIX = "Bearer ";

	private final byte[] secret;
	private final String issuer;
	private final String audience;
	private final Clock clock;

	ServiceTokenFilter(String secret, String issuer, String audience, Clock clock) {
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.issuer = issuer;
		this.audience = audience;
		this.clock = clock;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith(PREFIX)) {
			String principal = authenticate(header.substring(PREFIX.length()).trim());
			if (principal != null) {
				SecurityContextHolder.getContext()
					.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null,
							AuthorityUtils.createAuthorityList("ROLE_SERVICE")));
			}
		}
		chain.doFilter(request, response);
	}

	private String authenticate(String token) {
		try {
			SignedJWT jwt = SignedJWT.parse(token);
			if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(secret))) {
				return null;
			}
			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			Instant now = clock.instant();
			List<String> audiences = claims.getAudience();
			if (!issuer.equals(claims.getIssuer()) || audiences == null || !audiences.contains(audience)
					|| claims.getExpirationTime() == null || !now.isBefore(claims.getExpirationTime().toInstant())
					|| claims.getExpirationTime().toInstant().isAfter(now.plus(MAX_LIFETIME))) {
				return null;
			}
			String manager = claims.getStringClaim("manager");
			return manager == null || manager.isBlank() ? claims.getSubject() : manager;
		}
		catch (Exception ex) {
			return null;
		}
	}

}
