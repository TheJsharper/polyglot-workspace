package com.example.project00.security;

import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/** Verifies ES256 tokens signed offline with a private key that stays on the developer's machine. */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoTokenVerifier {

	private final ECPublicKey publicKey;
	private final Set<String> revokedIds;
	private final Clock clock;

	public DemoTokenVerifier(@Value("${app.demo.public-key:}") String publicKeyPem,
			@Value("${app.demo.revoked-jtis:}") List<String> revokedIds, Clock clock) {
		this.publicKey = parse(publicKeyPem);
		this.revokedIds = Set.copyOf(revokedIds.stream().map(String::trim).filter(id -> !id.isEmpty()).toList());
		this.clock = clock;
	}

	public Optional<JWTClaimsSet> verify(String token) {
		if (publicKey == null || token == null) {
			return Optional.empty();
		}
		try {
			SignedJWT jwt = SignedJWT.parse(token.trim());
			if (!JWSAlgorithm.ES256.equals(jwt.getHeader().getAlgorithm())
					|| !jwt.verify(new ECDSAVerifier(publicKey))) {
				return Optional.empty();
			}
			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			Instant now = clock.instant();
			if (claims.getExpirationTime() == null || !now.isBefore(claims.getExpirationTime().toInstant())) {
				return Optional.empty();
			}
			if (claims.getNotBeforeTime() != null && now.isBefore(claims.getNotBeforeTime().toInstant())) {
				return Optional.empty();
			}
			if (claims.getJWTID() == null || revokedIds.contains(claims.getJWTID())) {
				return Optional.empty();
			}
			return Optional.of(claims);
		}
		catch (Exception ex) {
			return Optional.empty();
		}
	}

	private static ECPublicKey parse(String pem) {
		if (pem == null || pem.isBlank()) {
			return null;
		}
		try {
			String body = pem.replace("\\n", "\n")
				.replaceAll("-----(BEGIN|END) PUBLIC KEY-----", "")
				.replaceAll("\\s", "");
			byte[] der = Base64.getDecoder().decode(body);
			return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
		}
		catch (Exception ex) {
			throw new IllegalStateException("app.demo.public-key is not a valid PEM-encoded EC public key", ex);
		}
	}

}
