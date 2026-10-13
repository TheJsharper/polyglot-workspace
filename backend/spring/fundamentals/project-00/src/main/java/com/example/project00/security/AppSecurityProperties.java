package com.example.project00.security;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Security settings bound from {@code app.security.*}.
 *
 * @param admin the hard-defined superior account
 * @param jwt token settings
 */
@Validated
@ConfigurationProperties("app.security")
public record AppSecurityProperties(@NotNull Admin admin, @NotNull Jwt jwt) {

	/**
	 * The single admin account; it lives only in configuration, never in the database.
	 *
	 * @param username login name
	 * @param password plain-text password, hashed with BCrypt at startup
	 */
	public record Admin(@NotBlank String username, @NotBlank String password) {
	}

	/**
	 * HS256 token settings.
	 *
	 * @param secret shared HMAC secret, at least 32 bytes (256 bits)
	 * @param ttl how long an issued token stays valid
	 * @param issuer value of the {@code iss} claim, verified on every request
	 */
	public record Jwt(@NotBlank @Size(min = 32) String secret, @NotNull Duration ttl, @NotBlank String issuer) {
	}

}
