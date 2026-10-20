package com.example.project00.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class TokenServiceTest {

	private static final String SECRET = "test-secret-0123456789-abcdefghijklmnopqrstuvwxyz";

	private final Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

	private TokenService tokenService;

	private NimbusJwtDecoder decoder;

	@BeforeEach
	void setUp() {
		SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		AppSecurityProperties properties = new AppSecurityProperties(
				new AppSecurityProperties.Admin("admin", "admin-pass"),
				new AppSecurityProperties.Jwt(SECRET, Duration.ofMinutes(30), "test-issuer"));
		tokenService = new TokenService(NimbusJwtEncoder.withSecretKey(key).build(), properties,
				Clock.fixed(now, ZoneOffset.UTC));
		decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("test-issuer"));
	}

	@Test
	void issuesSignedTokenWithSubjectRolesAndLifetime() {
		var authentication = UsernamePasswordAuthenticationToken.authenticated("S-1", null,
				AuthorityUtils.createAuthorityList("ROLE_STUDENT", "FACTOR_PASSWORD"));

		TokenService.IssuedToken token = tokenService.issue(authentication);
		Jwt jwt = decoder.decode(token.value());

		assertThat(token.expiresInSeconds()).isEqualTo(900);
		assertThat(jwt.getSubject()).isEqualTo("S-1");
		assertThat(jwt.getClaimAsString("iss")).isEqualTo("test-issuer");
		assertThat(jwt.getIssuedAt()).isEqualTo(now);
		assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
		assertThat(jwt.getClaimAsStringList(TokenService.ROLES_CLAIM)).containsExactly("STUDENT");
		assertThat(jwt.getHeaders()).containsEntry("alg", MacAlgorithm.HS256.getName());
	}

}
