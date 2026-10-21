package com.example.project00.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project00.domain.DemoKey;
import com.example.project00.repository.DemoKeyRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Creates, lists and deletes recruiter demo keys. Each key is its own ES256 keypair and a signed token whose
 * {@code kid} and {@code jti} are the key's id. Development only.
 */
@Service
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoKeyService {

	public static final int MIN_COUNT = 1;
	public static final int MAX_COUNT = 10;
	public static final int MIN_DAYS = 1;
	public static final int MAX_DAYS = 30;
	public static final int MAX_LABEL_LENGTH = 100;

	private final DemoKeyRepository keys;
	private final Clock clock;

	public DemoKeyService(DemoKeyRepository keys, Clock clock) {
		this.keys = keys;
		this.clock = clock;
	}

	/**
	 * Generates {@code count} independent keys, each with its own signed token.
	 * @param count how many keys to create, {@value #MIN_COUNT} to {@value #MAX_COUNT}
	 * @param days validity in days, {@value #MIN_DAYS} to {@value #MAX_DAYS}
	 * @param label a note for the manager, at most {@value #MAX_LABEL_LENGTH} characters; blank becomes "demo"
	 * @param createdBy the manager's username
	 * @return the created keys
	 * @throws IllegalArgumentException if an argument is out of range
	 */
	@Transactional
	public List<DemoKey> create(int count, int days, String label, String createdBy) {
		if (count < MIN_COUNT || count > MAX_COUNT) {
			throw new IllegalArgumentException("count must be between " + MIN_COUNT + " and " + MAX_COUNT);
		}
		if (days < MIN_DAYS || days > MAX_DAYS) {
			throw new IllegalArgumentException("days must be between " + MIN_DAYS + " and " + MAX_DAYS);
		}
		String cleanLabel = label == null || label.isBlank() ? "demo" : label.strip();
		if (cleanLabel.length() > MAX_LABEL_LENGTH) {
			throw new IllegalArgumentException("label must be at most " + MAX_LABEL_LENGTH + " characters");
		}
		Instant now = clock.instant();
		List<DemoKey> created = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			created.add(keys.save(newKey(cleanLabel, now, now.plus(Duration.ofDays(days)), createdBy)));
		}
		return created;
	}

	/** @return all keys, newest first */
	@Transactional(readOnly = true)
	public List<DemoKey> list() {
		return keys.findAllByOrderByCreatedAtDescIdDesc();
	}

	/**
	 * Deletes a key that was never redeemed.
	 * @param id the key id
	 * @throws ResourceNotFoundException if there is no such key
	 * @throws IllegalStateException if the key has been redeemed
	 */
	@Transactional
	public void deleteUnused(Long id) {
		DemoKey key = keys.findById(id).orElseThrow(() -> new ResourceNotFoundException("Demo key", id));
		if (key.isRedeemed()) {
			throw new IllegalStateException("A redeemed demo key cannot be deleted");
		}
		keys.delete(key);
	}

	/**
	 * Counts a successful redemption of the key with the given id.
	 * @param jti the token id
	 */
	@Transactional
	public void recordRedemption(String jti) {
		keys.findByJti(jti).ifPresent(key -> key.recordRedemption(clock.instant()));
	}

	private DemoKey newKey(String label, Instant now, Instant expiresAt, String createdBy) {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
			generator.initialize(new ECGenParameterSpec("secp256r1"));
			KeyPair pair = generator.generateKeyPair();
			String jti = UUID.randomUUID().toString();
			JWTClaimsSet claims = new JWTClaimsSet.Builder().jwtID(jti)
				.issueTime(Date.from(now))
				.notBeforeTime(Date.from(now))
				.expirationTime(Date.from(expiresAt))
				.claim("label", label)
				.build();
			SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jti).build(), claims);
			jwt.sign(new ECDSASigner((ECPrivateKey) pair.getPrivate()));
			return new DemoKey(jti, label, Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()),
					Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()), jwt.serialize(), now, now,
					expiresAt, createdBy);
		}
		catch (java.security.GeneralSecurityException | JOSEException ex) {
			throw new IllegalStateException("Could not generate a demo key", ex);
		}
	}

}
