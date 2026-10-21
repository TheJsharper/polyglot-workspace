package com.example.project00.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Asynchronous client for the demo-keys microservice (project-01). Every call runs on a virtual thread and returns
 * a {@link CompletableFuture}; each request carries a short-lived service token that names the acting manager.
 * Downstream failures are translated to 502/503/504 so internal details never reach the browser.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoKeysClient implements DisposableBean {

	private static final String BASE = "/internal/demo-keys";
	private static final int MIN_SECRET_LENGTH = 32;
	private static final Duration TOKEN_TTL = Duration.ofSeconds(60);

	/** A demo key as returned by the service. The private key is never part of it. */
	public record DemoKeyView(Long id, String label, String publicKey, String token, Instant createdAt,
			Instant notBefore, Instant expiresAt, boolean redeemed, Instant redeemedAt, int redeemCount,
			String createdBy) {
	}

	/** An asynchronous key-generation job. */
	public record DemoKeyJob(String id, String status, List<DemoKeyView> keys, String error, Instant createdAt) {
	}

	/** Outcome of verifying a token. */
	public record RedeemResult(boolean valid, String jti, Instant expiresAt) {
	}

	private final RestClient http;
	private final byte[] secret;
	private final String issuer;
	private final String audience;
	private final Clock clock;
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	public DemoKeysClient(Clock clock,
			@Value("${app.demo-keys.base-url:http://127.0.0.1:8081}") String baseUrl,
			@Value("${app.demo-keys.secret:}") String secret,
			@Value("${app.demo-keys.issuer:project-00}") String issuer,
			@Value("${app.demo-keys.audience:demo-keys-service}") String audience,
			@Value("${app.demo-keys.connect-timeout:2s}") Duration connectTimeout,
			@Value("${app.demo-keys.read-timeout:10s}") Duration readTimeout) {
		if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
			throw new IllegalStateException("app.demo-keys.secret (DEMO_KEYS_SERVICE_SECRET) must be at least "
					+ MIN_SECRET_LENGTH + " characters and match SERVICE_AUTH_SECRET of project-01");
		}
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeout);
		factory.setReadTimeout(readTimeout);
		this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.issuer = issuer;
		this.audience = audience;
		this.clock = clock;
	}

	/** Starts a key-generation job; completes with the accepted job and the URI to poll. */
	public CompletableFuture<DemoKeyJob> createJob(int count, int days, String label, String manager) {
		Map<String, Object> body = new java.util.HashMap<>();
		body.put("count", count);
		body.put("days", days);
		body.put("label", label);
		return async(() -> http.post()
			.uri(BASE + "/jobs")
			.headers(h -> h.setBearerAuth(serviceToken(manager)))
			.contentType(MediaType.APPLICATION_JSON)
			.body(body)
			.retrieve()
			.body(DemoKeyJob.class));
	}

	public CompletableFuture<DemoKeyJob> job(String id, String manager) {
		return async(() -> http.get()
			.uri(BASE + "/jobs/{id}", id)
			.headers(h -> h.setBearerAuth(serviceToken(manager)))
			.retrieve()
			.body(DemoKeyJob.class));
	}

	public CompletableFuture<List<DemoKeyView>> list(String manager) {
		return async(() -> http.get()
			.uri(BASE)
			.headers(h -> h.setBearerAuth(serviceToken(manager)))
			.retrieve()
			.body(new ParameterizedTypeReference<List<DemoKeyView>>() {
			}));
	}

	public CompletableFuture<Void> delete(Long id, String manager) {
		return async(() -> {
			http.delete().uri(BASE + "/{id}", id).headers(h -> h.setBearerAuth(serviceToken(manager))).retrieve()
				.toBodilessEntity();
			return null;
		});
	}

	/** Verifies a demo token and counts the redemption when it is valid. */
	public CompletableFuture<RedeemResult> redeem(String token) {
		return async(() -> http.post()
			.uri(BASE + "/redeem")
			.headers(h -> h.setBearerAuth(serviceToken("demo-redeem")))
			.contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("token", token))
			.retrieve()
			.body(RedeemResult.class));
	}

	private <T> CompletableFuture<T> async(Supplier<T> call) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return call.get();
			}
			catch (HttpStatusCodeException ex) {
				throw translate(ex.getStatusCode());
			}
			catch (ResourceAccessException ex) {
				boolean timeout = ex.getCause() instanceof java.net.SocketTimeoutException;
				throw new ResponseStatusException(timeout ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.SERVICE_UNAVAILABLE,
						timeout ? "The demo key service timed out." : "The demo key service is unavailable.");
			}
		}, executor);
	}

	private static ResponseStatusException translate(HttpStatusCode status) {
		return switch (status.value()) {
			case 400 -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid demo key request.");
			case 404 -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demo key or job not found.");
			case 409 -> new ResponseStatusException(HttpStatus.CONFLICT, "A redeemed demo key cannot be deleted.");
			case 503 -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"The demo key service is busy. Try again shortly.");
			// Includes 401/403, which mean the shared secret is misconfigured: never forwarded as-is.
			default -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The demo key service failed.");
		};
	}

	private String serviceToken(String manager) {
		Instant now = clock.instant();
		try {
			JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(issuer)
				.audience(audience)
				.subject(issuer)
				.claim("manager", manager)
				.issueTime(Date.from(now))
				.expirationTime(Date.from(now.plus(TOKEN_TTL)))
				.build();
			SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
			jwt.sign(new MACSigner(secret));
			return jwt.serialize();
		}
		catch (JOSEException ex) {
			throw new IllegalStateException("Could not sign the service token", ex);
		}
	}

	@Override
	public void destroy() {
		executor.shutdown();
	}

}
