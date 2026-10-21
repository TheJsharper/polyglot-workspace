package com.example.project00.security;

import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.project00.domain.DemoKey;
import com.example.project00.repository.DemoKeyRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Verifies ES256 demo tokens against the public key stored for the token's {@code kid}. Deleting a key from the
 * database revokes its token.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoTokenVerifier {

	private final DemoKeyRepository keys;
	private final Clock clock;

	public DemoTokenVerifier(DemoKeyRepository keys, Clock clock) {
		this.keys = keys;
		this.clock = clock;
	}

	public Optional<JWTClaimsSet> verify(String token) {
		if (token == null) {
			return Optional.empty();
		}
		try {
			SignedJWT jwt = SignedJWT.parse(token.trim());
			String keyId = jwt.getHeader().getKeyID();
			if (!JWSAlgorithm.ES256.equals(jwt.getHeader().getAlgorithm()) || keyId == null) {
				return Optional.empty();
			}
			DemoKey key = keys.findByJti(keyId).orElse(null);
			if (key == null || !jwt.verify(new ECDSAVerifier(publicKey(key)))) {
				return Optional.empty();
			}
			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			Instant now = clock.instant();
			if (!keyId.equals(claims.getJWTID()) || claims.getExpirationTime() == null
					|| !now.isBefore(claims.getExpirationTime().toInstant()) || !now.isBefore(key.getExpiresAt())) {
				return Optional.empty();
			}
			if (claims.getNotBeforeTime() != null && now.isBefore(claims.getNotBeforeTime().toInstant())) {
				return Optional.empty();
			}
			return Optional.of(claims);
		}
		catch (Exception ex) {
			return Optional.empty();
		}
	}

	private static ECPublicKey publicKey(DemoKey key) throws java.security.GeneralSecurityException {
		byte[] der = Base64.getDecoder().decode(key.getPublicKey());
		return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
	}

}
