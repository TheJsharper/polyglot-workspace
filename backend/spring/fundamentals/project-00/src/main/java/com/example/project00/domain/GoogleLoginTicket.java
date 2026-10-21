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

	@Column(name = "picture_url", length = 2048)
	private String pictureUrl;

	@Column(name = "binding_hash", length = 64)
	private String bindingHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean consumed;

	public GoogleLoginTicket(String tokenHash, String googleSubject, String email, String displayName, Instant expiresAt) {
		this(tokenHash, googleSubject, email, displayName, null, null, expiresAt);
	}

	public GoogleLoginTicket(String tokenHash, String googleSubject, String email, String displayName,
			String pictureUrl, Instant expiresAt) {
		this(tokenHash, googleSubject, email, displayName, pictureUrl, null, expiresAt);
	}

	/**
	 * @param bindingHash hash of a secret held only by the browser that completed the Google flow; completion
	 * requires the same secret, so a ticket obtained elsewhere cannot sign another browser in
	 */
	public GoogleLoginTicket(String tokenHash, String googleSubject, String email, String displayName,
			String pictureUrl, String bindingHash, Instant expiresAt) {
		this.tokenHash = Objects.requireNonNull(tokenHash);
		this.googleSubject = Objects.requireNonNull(googleSubject);
		this.email = Objects.requireNonNull(email);
		this.displayName = Objects.requireNonNull(displayName);
		this.pictureUrl = pictureUrl;
		this.bindingHash = bindingHash;
		this.expiresAt = Objects.requireNonNull(expiresAt);
	}

	public void consume() {
		this.consumed = true;
	}
}
