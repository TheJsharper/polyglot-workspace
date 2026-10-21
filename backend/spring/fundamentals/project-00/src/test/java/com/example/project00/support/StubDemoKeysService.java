package com.example.project00.support;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Minimal in-memory stand-in for the demo-keys microservice (project-01). It enforces the same service-token rules,
 * so tests also prove that project-00 signs its calls correctly. Jobs finish after {@code pollsUntilDone} polls.
 */
public final class StubDemoKeysService {

	public static final String SECRET = "test-only-service-secret-0123456789-abcdefghijkl";
	public static final String VALID_TOKEN = "valid-demo-token";

	private static final String BASE = "/internal/demo-keys";
	private static final Pattern COUNT = Pattern.compile("\"count\"\\s*:\\s*(\\d+)");

	private static StubDemoKeysService instance;

	private final HttpServer server;
	private final AtomicLong ids = new AtomicLong(1);
	private final Map<String, Integer> pollsLeft = new ConcurrentHashMap<>();
	private final Map<String, String> jobs = new ConcurrentHashMap<>();
	private final Map<Long, Boolean> keys = new ConcurrentHashMap<>();
	/** The manager claim of every authenticated call, in order. */
	public final List<String> managers = new CopyOnWriteArrayList<>();

	private StubDemoKeysService() throws IOException {
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", this::handle);
		server.start();
		keys.put(900L, true);
	}

	public static synchronized StubDemoKeysService shared() {
		if (instance == null) {
			try {
				instance = new StubDemoKeysService();
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}
		return instance;
	}

	public String baseUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort();
	}

	/** Id of a key the stub treats as already redeemed. */
	public long redeemedKeyId() {
		return 900L;
	}

	private void handle(HttpExchange ex) throws IOException {
		try {
			String manager = authenticate(ex.getRequestHeaders().getFirst("Authorization"));
			if (manager == null) {
				send(ex, 401, "{}");
				return;
			}
			managers.add(manager);
			String path = ex.getRequestURI().getPath();
			String method = ex.getRequestMethod();
			String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
			if (method.equals("POST") && path.equals(BASE + "/jobs")) {
				createJob(ex, body, manager);
			}
			else if (method.equals("GET") && path.startsWith(BASE + "/jobs/")) {
				getJob(ex, path.substring((BASE + "/jobs/").length()));
			}
			else if (method.equals("GET") && path.equals(BASE)) {
				send(ex, 200, "[" + String.join(",", keys.keySet().stream().sorted().map(id -> keyJson(id, manager)).toList()) + "]");
			}
			else if (method.equals("DELETE") && path.startsWith(BASE + "/")) {
				delete(ex, path.substring(BASE.length() + 1));
			}
			else if (method.equals("POST") && path.equals(BASE + "/redeem")) {
				boolean valid = body.contains("\"" + VALID_TOKEN + "\"");
				send(ex, 200, valid ? "{\"valid\":true,\"jti\":\"jti-1\",\"expiresAt\":\""
						+ Instant.now().plusSeconds(3600) + "\"}" : "{\"valid\":false}");
			}
			else {
				send(ex, 404, "{}");
			}
		}
		catch (RuntimeException ex2) {
			send(ex, 500, "{}");
		}
	}

	private void createJob(HttpExchange ex, String body, String manager) throws IOException {
		Matcher matcher = COUNT.matcher(body);
		int count = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
		String id = java.util.UUID.randomUUID().toString();
		List<String> created = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			long keyId = ids.getAndIncrement();
			keys.put(keyId, false);
			created.add(keyJson(keyId, manager));
		}
		jobs.put(id, String.join(",", created));
		pollsLeft.put(id, 1);
		ex.getResponseHeaders().add("Location", BASE + "/jobs/" + id);
		send(ex, 202, jobJson(id, "PENDING", ""));
	}

	private void getJob(HttpExchange ex, String id) throws IOException {
		String keysJson = jobs.get(id);
		if (keysJson == null) {
			send(ex, 404, "{}");
			return;
		}
		int left = pollsLeft.merge(id, -1, Integer::sum);
		send(ex, 200, left >= 0 ? jobJson(id, "RUNNING", "") : jobJson(id, "DONE", keysJson));
	}

	private void delete(HttpExchange ex, String rawId) throws IOException {
		Long id = Long.valueOf(rawId);
		Boolean redeemed = keys.get(id);
		if (redeemed == null) {
			send(ex, 404, "{}");
		}
		else if (redeemed) {
			send(ex, 409, "{}");
		}
		else {
			keys.remove(id);
			ex.sendResponseHeaders(204, -1);
			ex.close();
		}
	}

	private static String jobJson(String id, String status, String keysJson) {
		return "{\"id\":\"" + id + "\",\"status\":\"" + status + "\",\"keys\":[" + keysJson
				+ "],\"error\":null,\"createdAt\":\"" + Instant.now() + "\"}";
	}

	private static String keyJson(long id, String manager) {
		return "{\"id\":" + id + ",\"label\":\"recruiter\",\"publicKey\":\"pk\",\"token\":\"tok-" + id
				+ "\",\"createdAt\":\"" + Instant.now() + "\",\"notBefore\":\"" + Instant.now()
				+ "\",\"expiresAt\":\"" + Instant.now().plusSeconds(86400) + "\",\"redeemed\":false,\"redeemedAt\":null,"
				+ "\"redeemCount\":0,\"createdBy\":\"" + manager + "\"}";
	}

	private static void send(HttpExchange ex, int status, String json) throws IOException {
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json");
		ex.sendResponseHeaders(status, bytes.length);
		ex.getResponseBody().write(bytes);
		ex.close();
	}

	private static String authenticate(String header) {
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		try {
			SignedJWT jwt = SignedJWT.parse(header.substring(7));
			if (!jwt.verify(new MACVerifier(SECRET.getBytes(StandardCharsets.UTF_8)))) {
				return null;
			}
			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			Date exp = claims.getExpirationTime();
			if (!"project-00".equals(claims.getIssuer()) || !claims.getAudience().contains("demo-keys-service")
					|| exp == null || exp.toInstant().isBefore(Instant.now())
					|| exp.toInstant().isAfter(Instant.now().plusSeconds(300))) {
				return null;
			}
			return claims.getStringClaim("manager");
		}
		catch (Exception ex) {
			return null;
		}
	}

}
