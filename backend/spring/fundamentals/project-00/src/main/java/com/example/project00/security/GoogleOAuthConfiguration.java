package com.example.project00.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.core.env.Environment;
import com.example.project00.service.GoogleLoginService;

@Configuration
@ConditionalOnProperty(name = "app.google.enabled", havingValue = "true")
class GoogleOAuthConfiguration {

	@Bean
	GoogleLoginSuccessHandler googleLoginSuccessHandler(GoogleLoginService loginService,
			GoogleOAuthProperties properties) {
		return new GoogleLoginSuccessHandler(loginService, properties);
	}

	@Bean
	ClientRegistrationRepository clientRegistrationRepository(GoogleOAuthProperties properties, Environment environment) {
		if (properties.clientId() == null || properties.clientId().isBlank()
				|| properties.clientSecret() == null || properties.clientSecret().isBlank()) {
			throw new IllegalStateException("GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET are required when Google SSO is enabled");
		}
		if (environment.matchesProfiles("prod")
				&& (!properties.redirectUri().startsWith("https://") || !properties.frontendUrl().startsWith("https://"))) {
			throw new IllegalStateException("Google callback and frontend URLs must use HTTPS in production");
		}
		ClientRegistration google = ClientRegistration.withRegistrationId("google")
			.clientId(properties.clientId())
			.clientSecret(properties.clientSecret())
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri(properties.redirectUri())
			.scope("openid", "email", "profile")
			.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
			.tokenUri("https://oauth2.googleapis.com/token")
			.userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
			.userNameAttributeName("sub")
			.clientName("Google")
			.issuerUri("https://accounts.google.com")
			.build();
		return new InMemoryClientRegistrationRepository(google);
	}
}
