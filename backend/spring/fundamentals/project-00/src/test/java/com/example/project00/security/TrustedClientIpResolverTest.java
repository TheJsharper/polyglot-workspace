package com.example.project00.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.HttpServletRequestWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class TrustedClientIpResolverTest {

	private static final String CLIENT_IP = "198.51.100.27";
	private static final String SECRET = "test-shared-secret";

	private final TrustedClientIpResolver resolver = new TrustedClientIpResolver(SECRET);

	@Test
	void trustsForwardedIpWhenSecretMatches() {
		MockHttpServletRequest request = request();
		request.addHeader(TrustedClientIpResolver.CLIENT_IP_HEADER, CLIENT_IP);
		request.addHeader(TrustedClientIpResolver.PROXY_SECRET_HEADER, SECRET);

		assertThat(resolver.resolve(request)).isEqualTo(CLIENT_IP);
	}

	@Test
	void usesSocketAddressWhenSecretIsMissingOrDoesNotMatch() {
		MockHttpServletRequest missingSecret = request();
		missingSecret.addHeader(TrustedClientIpResolver.CLIENT_IP_HEADER, CLIENT_IP);
		MockHttpServletRequest mismatchedSecret = request();
		mismatchedSecret.addHeader(TrustedClientIpResolver.CLIENT_IP_HEADER, CLIENT_IP);
		mismatchedSecret.addHeader(TrustedClientIpResolver.PROXY_SECRET_HEADER, "wrong-secret");

		assertThat(resolver.resolve(missingSecret)).isEqualTo("192.0.2.15");
		assertThat(resolver.resolve(mismatchedSecret)).isEqualTo("192.0.2.15");
	}

	@Test
	void usesSocketAddressWhenNoSecretIsConfigured() {
		MockHttpServletRequest request = request();
		request.addHeader(TrustedClientIpResolver.CLIENT_IP_HEADER, CLIENT_IP);
		request.addHeader(TrustedClientIpResolver.PROXY_SECRET_HEADER, SECRET);

		assertThat(new TrustedClientIpResolver("").resolve(request)).isEqualTo("192.0.2.15");
	}

	@Test
	void unwrapsRequestBeforeReadingSocketAddress() {
		MockHttpServletRequest request = request();
		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
			@Override
			public String getRemoteAddr() {
				return CLIENT_IP;
			}
		};

		assertThat(resolver.resolve(wrapper)).isEqualTo("192.0.2.15");
	}

	private MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("192.0.2.15");
		return request;
	}
}
