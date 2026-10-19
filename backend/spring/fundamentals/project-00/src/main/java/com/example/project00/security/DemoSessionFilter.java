package com.example.project00.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Restricts recruiter demo sessions (JWT claim {@code demo=true}) to read-only requests and rate limits them.
 * Runs after bearer authentication; other callers pass through untouched.
 */
class DemoSessionFilter extends OncePerRequestFilter {

	private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

	private final SlidingWindowLimiter requests;

	DemoSessionFilter(Clock clock, int maxRequestsPerMinute) {
		this.requests = new SlidingWindowLimiter(maxRequestsPerMinute, Duration.ofMinutes(1), clock);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwt
				&& Boolean.TRUE.equals(jwt.getToken().getClaims().get(TokenService.DEMO_CLAIM))) {
			if (!SAFE_METHODS.contains(request.getMethod())) {
				reject(response, HttpStatus.FORBIDDEN, "Demo sessions are read-only.");
				return;
			}
			if (!requests.tryAcquire(jwt.getName())) {
				response.setHeader("Retry-After", "60");
				reject(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded for the demo session.");
				return;
			}
		}
		chain.doFilter(request, response);
	}

	private static void reject(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter()
			.write("{\"status\":" + status.value() + ",\"title\":\"" + status.getReasonPhrase() + "\",\"detail\":\""
					+ detail + "\"}");
	}

}
