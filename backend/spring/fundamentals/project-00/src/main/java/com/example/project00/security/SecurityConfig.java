package com.example.project00.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Stateless JWT security.
 * <ul>
 * <li>Public: {@code POST /api/auth/register} (student or professor; registering an admin needs an admin token),
 * {@code POST /api/auth/login}, {@code /hello} and the OpenAPI /
 * Swagger UI resources.</li>
 * <li>Everything else requires a valid {@code Authorization: Bearer <token>} header.</li>
 * <li>Role and ownership rules are declared with {@code @PreAuthorize} on the controllers, using
 * {@link AuthorizationRules} ({@code @authz}).</li>
 * </ul>
 * 401 and 403 responses are rendered as {@code application/problem+json} by the MVC exception handler, so they
 * look like every other API error.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({ AppSecurityProperties.class, GoogleOAuthProperties.class })
class SecurityConfig {

	@Bean
	SecurityFilterChain apiSecurity(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter,
			AuthenticationEntryPoint entryPoint, AccessDeniedHandler accessDeniedHandler, Clock clock,
			Environment environment,
			GoogleOAuthProperties googleProperties,
			ObjectProvider<GoogleLoginSuccessHandler> googleSuccessHandler,
			@Value("${app.demo.enabled:false}") boolean demoEnabled,
			@Value("${app.demo.max-requests-per-minute:60}") int demoRequestsPerMinute) throws Exception {
		boolean devDemoEnabled = demoEnabled && environment.matchesProfiles("dev");
		CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
		csrfTokenRepository.setCookieCustomizer(cookie -> cookie.httpOnly(true)
				.sameSite("Lax")
				.secure(environment.matchesProfiles("prod")));
		if (devDemoEnabled) {
			http.addFilterAfter(new DemoSessionFilter(clock, demoRequestsPerMinute),
					BearerTokenAuthenticationFilter.class);
		}
		http.csrf(csrf -> csrf
				.csrfTokenRepository(csrfTokenRepository)
				.requireCsrfProtectionMatcher(request -> "POST".equals(request.getMethod())
						&& ("/api/auth/refresh".equals(request.getServletPath())
								|| "/api/auth/logout".equals(request.getServletPath()))))
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
			.httpBasic(basic -> basic.disable())
			.formLogin(form -> form.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.authorizeHttpRequests(requests -> requests
				.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login")
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/api/auth/google/status", "/api/auth/google/start", "/api/auth/csrf")
				.permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/google/complete", "/api/auth/refresh", "/api/auth/logout")
				.permitAll()
				.requestMatchers("/oauth2/**", "/login/oauth2/**")
				.permitAll()
				.requestMatchers(devDemoEnabled ? HttpMethod.POST : HttpMethod.TRACE, "/api/demo/redeem")
				.permitAll()
				.requestMatchers(devDemoEnabled ? HttpMethod.GET : HttpMethod.TRACE, "/api/demo/status")
				.permitAll()
				.requestMatchers("/hello", "/error", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html",
						"/swagger-ui/**", "/h2-console", "/h2-console/**")
				.permitAll()
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer(resourceServer -> resourceServer
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
				.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDeniedHandler));
		if (googleProperties.enabled()) {
			http.oauth2Login(oauth -> oauth
				.successHandler(googleSuccessHandler.getObject())
				.failureHandler((request, response, exception) -> response.sendRedirect(
						googleProperties.frontendUrl() + "?error=google_signin_failed")));
		}
		return http.build();
	}

	// Delegating to the MVC resolver lets ApiExceptionHandler render security failures as ProblemDetail.
	@Bean
	AuthenticationEntryPoint problemDetailEntryPoint(
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		return (request, response, exception) -> resolver.resolveException(request, response, null, exception);
	}

	@Bean
	AccessDeniedHandler problemDetailAccessDeniedHandler(
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		return (request, response, exception) -> resolver.resolveException(request, response, null, exception);
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName(TokenService.ROLES_CLAIM);
		authorities.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	@Bean
	JwtEncoder jwtEncoder(AppSecurityProperties properties) {
		return NimbusJwtEncoder.withSecretKey(secretKey(properties)).build();
	}

	@Bean
	JwtDecoder jwtDecoder(AppSecurityProperties properties,
			@Value("${app.demo.enabled:false}") boolean demoEnabled, Environment environment) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties))
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
		OAuth2TokenValidator<Jwt> rejectLegacyDemoSessions = jwt -> Boolean.TRUE.equals(jwt.getClaim("demo"))
				&& !(demoEnabled && environment.matchesProfiles("dev"))
				? OAuth2TokenValidatorResult.failure(
						new OAuth2Error("invalid_token", "Demo sessions are only accepted in development mode", null))
				: OAuth2TokenValidatorResult.success();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.jwt().issuer()), rejectLegacyDemoSessions));
		return decoder;
	}

	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	private static SecretKey secretKey(AppSecurityProperties properties) {
		return new SecretKeySpec(properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

}
