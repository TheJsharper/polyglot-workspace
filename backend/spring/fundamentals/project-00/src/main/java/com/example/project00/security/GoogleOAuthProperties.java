package com.example.project00.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.google")
public record GoogleOAuthProperties(boolean enabled, String clientId, String clientSecret, String frontendUrl,
		String redirectUri) {
}
