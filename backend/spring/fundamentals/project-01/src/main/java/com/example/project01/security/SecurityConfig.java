package com.example.project01.security;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Every route requires a valid service token; nothing is public. */
@Configuration
class SecurityConfig {

	private static final int MIN_SECRET_LENGTH = 32;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, Clock clock,
			@Value("${app.service-auth.secret}") String secret, @Value("${app.service-auth.issuer}") String issuer,
			@Value("${app.service-auth.audience}") String audience) {
		if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
			throw new IllegalStateException(
					"app.service-auth.secret (SERVICE_AUTH_SECRET) must be at least " + MIN_SECRET_LENGTH + " characters");
		}
		// CSRF is irrelevant: no cookies or sessions, only an explicit bearer token that browsers never attach.
		return http.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.headers(Customizer.withDefaults())
			.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
			.authorizeHttpRequests(a -> a.requestMatchers("/internal/**").hasRole("SERVICE").anyRequest().denyAll())
			.addFilterBefore(new ServiceTokenFilter(secret, issuer, audience, clock),
					UsernamePasswordAuthenticationFilter.class)
			.build();
	}

}
