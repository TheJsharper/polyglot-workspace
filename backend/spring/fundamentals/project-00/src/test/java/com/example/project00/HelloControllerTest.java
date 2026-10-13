package com.example.project00;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

// Loads the application's SecurityConfig so the slice applies the real rules (/hello is public).
@WebMvcTest(controllers = HelloController.class, includeFilters = @Filter(type = FilterType.REGEX,
		pattern = "com\\.example\\.project00\\.security\\.SecurityConfig"))
class HelloControllerTest {

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void helloReturnsGreetingWithoutToken() {
		assertThat(mvc.get().uri("/hello")).hasStatusOk().bodyText().isEqualTo("Hello from Spring Boot!");
	}

	@Test
	void helloRejectsUnsupportedMethod() {
		assertThat(mvc.post().uri("/hello")).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	void unknownPathRequiresAuthentication() {
		assertThat(mvc.get().uri("/missing")).hasStatus(HttpStatus.UNAUTHORIZED)
			.headers()
			.hasValue("WWW-Authenticate", "Bearer");
	}

	@Test
	@WithMockUser
	void unknownPathReturnsNotFoundForAuthenticatedCaller() {
		assertThat(mvc.get().uri("/missing")).hasStatus(HttpStatus.NOT_FOUND);
	}

}