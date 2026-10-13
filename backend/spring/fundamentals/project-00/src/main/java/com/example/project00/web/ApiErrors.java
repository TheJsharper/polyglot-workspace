package com.example.project00.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ProblemDetail;

/**
 * Reusable OpenAPI response declarations for the RFC 9457 error bodies produced by {@link ApiExceptionHandler}.
 */
final class ApiErrors {

	static final String PROBLEM_JSON = "application/problem+json";

	private ApiErrors() {
	}

	/** Documents a {@code 400 Bad Request} problem response. */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponse(responseCode = "400", description = "Invalid request body or broken domain rule",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
	@interface BadRequest {

	}

	/** Documents a {@code 404 Not Found} problem response. */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponse(responseCode = "404", description = "A referenced resource does not exist",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
	@interface NotFound {

	}

	/** Documents a {@code 409 Conflict} problem response. */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponse(responseCode = "409", description = "Duplicate business key or resource still in use",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
	@interface Conflict {

	}

	/** Documents a {@code 401 Unauthorized} problem response. */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponse(responseCode = "401", description = "Missing, invalid or expired bearer token, or bad credentials",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
	@interface Unauthorized {

	}

	/** Documents a {@code 403 Forbidden} problem response. */
	@Documented
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponse(responseCode = "403", description = "The caller's role or identity does not allow this operation",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
	@interface Forbidden {

	}

}
