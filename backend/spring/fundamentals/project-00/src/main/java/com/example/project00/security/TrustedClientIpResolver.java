package com.example.project00.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;

/** Resolves the client IP forwarded by Pages only when its shared secret matches. */
@Component
public class TrustedClientIpResolver {

	static final String CLIENT_IP_HEADER = "X-Trusted-Client-IP";
	static final String PROXY_SECRET_HEADER = "X-Proxy-Shared-Secret";

	private final byte[] configuredSecret;

	public TrustedClientIpResolver(@Value("${app.trusted-client-ip.shared-secret:}") String sharedSecret) {
		this.configuredSecret = sharedSecret.getBytes(StandardCharsets.UTF_8);
	}

	public String resolve(HttpServletRequest request) {
		String forwardedIp = request.getHeader(CLIENT_IP_HEADER);
		String suppliedSecret = request.getHeader(PROXY_SECRET_HEADER);
		if (configuredSecret.length > 0 && forwardedIp != null && !forwardedIp.isBlank()
				&& suppliedSecret != null
				&& MessageDigest.isEqual(configuredSecret, suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
			return forwardedIp;
		}
		return socketRemoteAddress(request);
	}

	private String socketRemoteAddress(HttpServletRequest request) {
		ServletRequest current = request;
		while (current instanceof ServletRequestWrapper wrapper) {
			current = wrapper.getRequest();
		}
		return ((HttpServletRequest) current).getRemoteAddr();
	}
}
