package com.example.project00.web;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DemoDisabledE2ETest extends EndToEndTestSupport {

	@Test
	void demoEndpointsDoNotExistOutsideTheDemoEnvironment() {
		anonymous.post()
			.uri("/api/demo/redeem")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Map.of("token", "DEMO-AAAAAA", "persona", "STUDENT"))
			.exchange()
			.expectStatus()
			.isUnauthorized();
		client.post()
			.uri("/api/demo-tokens")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Map.of("label", "HR"))
			.exchange()
			.expectStatus()
			.isNotFound();
	}

}
