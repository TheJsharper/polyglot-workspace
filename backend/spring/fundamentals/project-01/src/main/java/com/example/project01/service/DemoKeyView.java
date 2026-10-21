package com.example.project01.service;

import java.time.Instant;

import com.example.project01.domain.DemoKey;

/** A demo key as exposed over the API. The private key is never included. */
public record DemoKeyView(Long id, String label, String publicKey, String token, Instant createdAt,
		Instant notBefore, Instant expiresAt, boolean redeemed, Instant redeemedAt, int redeemCount,
		String createdBy) {

	public static DemoKeyView of(DemoKey key) {
		return new DemoKeyView(key.getId(), key.getLabel(), key.getPublicKey(), key.getToken(), key.getCreatedAt(),
				key.getNotBefore(), key.getExpiresAt(), key.isRedeemed(), key.getRedeemedAt(),
				key.getRedeemCount(), key.getCreatedBy());
	}

}
