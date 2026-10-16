package com.example.project00.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OpenApiDocsE2ETest extends EndToEndTestSupport {

	@Test
	void apiDocsDescribeTheApi() {
		anonymous.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.info.title")
			.isEqualTo("University API")
			.jsonPath("$.info.version")
			.isEqualTo("v1")
			.jsonPath("$.paths['/api/courses'].post")
			.exists()
			.jsonPath("$.paths['/api/lectures/{lectureId}/attendees/{studentId}'].put")
			.exists()
			.jsonPath("$.paths['/api/notes/pending-review'].get")
			.exists()
			.jsonPath("$.paths['/hello']")
			.doesNotExist();
	}

	@Test
	void apiDocsIncludeErrorResponsesAndSchemas() {
		anonymous.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.paths['/api/courses/{courseCode}'].get.responses['404']")
			.exists()
			.jsonPath("$.paths['/api/courses'].post.responses['409']")
			.exists()
			.jsonPath("$.paths['/api/courses'].post.responses['400']")
			.exists()
			.jsonPath("$.components.schemas.CreateCourseRequest.properties.courseCode.example")
			.isEqualTo("CS101")
			.jsonPath("$.components.schemas.NotesResponse.description")
			.isEqualTo("Notes written by a student during a lecture");
	}

	@Test
	void apiDocsDescribeBearerSecurity() {
		anonymous.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.components.securitySchemes.bearerAuth.type")
			.isEqualTo("http")
			.jsonPath("$.components.securitySchemes.bearerAuth.scheme")
			.isEqualTo("bearer")
			.jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat")
			.isEqualTo("JWT")
			.jsonPath("$.security[0].bearerAuth")
			.exists()
			.jsonPath("$.paths['/api/auth/login'].post.security.length()")
			.isEqualTo(0)
			.jsonPath("$.paths['/api/auth/register'].post.security.length()")
			.isEqualTo(0)
			.jsonPath("$.paths['/api/auth/login'].post.responses['401']")
			.exists()
			.jsonPath("$.paths['/api/courses'].post.responses['401']")
			.exists()
			.jsonPath("$.paths['/api/courses'].post.responses['403']")
			.exists()
			.jsonPath("$.paths['/api/courses'].get.responses['403']")
			.doesNotExist();
	}

	@Test
	void swaggerUiIsServed() {
		anonymous.get()
			.uri("/swagger-ui/index.html")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentTypeCompatibleWith(MediaType.TEXT_HTML);
	}

	@Test
	void h2ConsoleIsAccessibleWithoutAuthentication() {
		anonymous.get()
			.uri("/h2-console/")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.valueEquals("X-Frame-Options", "SAMEORIGIN")
			.expectBody();
	}

}
