package com.example.project00.domain;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "google_login_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleLoginTicket extends BaseEntity {

	@Column(name = "token_hash", nullable = false, unique = true, length = 64)
	private String tokenHash;

	@Column(name = "google_subject", nullable = false, length = 255)
	private String googleSubject;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(name = "display_name", nullable = false, length = 254)
	private String displayName;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean consumed;

	public GoogleLoginTicket(String tokenHash, String googleSubject, String email, String displayName, Instant expiresAt) {
		this.tokenHash = Objects.requireNonNull(tokenHash);
		this.googleSubject = Objects.requireNonNull(googleSubject);
		this.email = Objects.requireNonNull(email);
		this.displayName = Objects.requireNonNull(displayName);
		this.expiresAt = Objects.requireNonNull(expiresAt);
	}

	public void consume() {
		this.consumed = true;
	}
}
