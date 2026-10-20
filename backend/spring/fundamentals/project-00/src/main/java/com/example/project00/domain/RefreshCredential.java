package com.example.project00.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshCredential extends BaseEntity {

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "family_id", nullable = false)
	private UUID familyId;

	@Column(nullable = false, length = 254)
	private String username;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	public RefreshCredential(String tokenHash, UUID familyId, String username, Instant expiresAt) {
		this.tokenHash = Objects.requireNonNull(tokenHash);
		this.familyId = Objects.requireNonNull(familyId);
		this.username = Objects.requireNonNull(username);
		this.expiresAt = Objects.requireNonNull(expiresAt);
	}

	public boolean activeAt(Instant now) {
		return revokedAt == null && expiresAt.isAfter(now);
	}

	public void revoke(Instant now) {
		if (revokedAt == null) {
			revokedAt = now;
		}
	}
}
