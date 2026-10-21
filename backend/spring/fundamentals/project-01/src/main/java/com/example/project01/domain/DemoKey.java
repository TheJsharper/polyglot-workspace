package com.example.project01.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A recruiter demo key: an ES256 keypair and the token signed with it. The private key never leaves the database
 * and is not exposed through any API.
 */
@Getter
@Entity
@Table(name = "demo_keys")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoKey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 36)
	private String jti;

	@Column(nullable = false, length = 100)
	private String label;

	/** Base64 of the X.509 (SubjectPublicKeyInfo) encoding. */
	@Column(name = "public_key", nullable = false, length = 512)
	private String publicKey;

	/** Base64 of the PKCS#8 encoding. */
	@Column(name = "private_key", nullable = false, length = 512)
	private String privateKey;

	@Column(nullable = false, length = 2048)
	private String token;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "not_before", nullable = false)
	private Instant notBefore;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "redeemed_at")
	private Instant redeemedAt;

	@Column(name = "redeem_count", nullable = false)
	private int redeemCount;

	@Column(name = "created_by", nullable = false, length = 254)
	private String createdBy;

	@SuppressWarnings("java:S107")
	public DemoKey(String jti, String label, String publicKey, String privateKey, String token, Instant createdAt,
			Instant notBefore, Instant expiresAt, String createdBy) {
		this.jti = jti;
		this.label = label;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.token = token;
		this.createdAt = createdAt;
		this.notBefore = notBefore;
		this.expiresAt = expiresAt;
		this.createdBy = createdBy;
	}

	/** @return whether the token has been redeemed at least once */
	public boolean isRedeemed() {
		return redeemedAt != null;
	}

	/**
	 * Records a successful redemption.
	 * @param at when the token was redeemed
	 */
	public void recordRedemption(Instant at) {
		if (redeemedAt == null) {
			redeemedAt = at;
		}
		redeemCount++;
	}

}
