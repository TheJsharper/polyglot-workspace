package com.example.project00.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Global OpenAPI metadata shown by Swagger UI at {@code /swagger-ui.html}; the raw specification is served at
 * {@code /v3/api-docs}.
 * <p>
 * Declares the {@value #BEARER_AUTH} JWT scheme as a global requirement, so Swagger UI's <em>Authorize</em> button
 * attaches the token to every call. Public endpoints opt out with an empty {@code @SecurityRequirements}.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

	static final String BEARER_AUTH = "bearerAuth";

	@Bean
	OpenAPI universityOpenApi() {
		return new OpenAPI()
			.info(new Info().title("University API")
				.version("v1")
				.description("""
						Manages courses, professors, students, the lectures that connect them, and the notes \
						students write during lectures.

						**Authentication:** students register themselves with `POST /api/auth/register`; \
						professors are created by the admin. Get a token from `POST /api/auth/login`, then click \
						**Authorize** and paste it.

						| Role | May |
						|---|---|
						| ADMIN | manage courses and professors, assign teachers, delete students |
						| PROFESSOR | schedule and run lectures and review notes for the courses they teach |
						| STUDENT | enroll in courses and manage their own notes |

						Errors are returned as RFC 9457 `application/problem+json` documents:
						* **400** – request validation failed or a domain rule was broken
						* **401** – missing, invalid or expired bearer token, or bad credentials
						* **403** – the caller's role or identity does not allow the operation
						* **404** – a referenced resource does not exist
						* **409** – a duplicate business key, or the resource is still in use""")
				.license(new License().name("Learning project")))
			.components(new Components().addSecuritySchemes(BEARER_AUTH,
					new SecurityScheme().type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Access token from POST /api/auth/login")))
			.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
			.addTagsItem(new Tag().name("Authentication").description("Self-registration, login and the current user"))
			.addTagsItem(new Tag().name("Courses").description("Course catalogue, enrollment and teaching assignments"))
			.addTagsItem(new Tag().name("Professors").description("Teaching staff, identified by employee id"))
			.addTagsItem(new Tag().name("Students").description("Enrolled students, identified by student id"))
			.addTagsItem(new Tag().name("Lectures").description("Scheduled lectures and their attendance"))
			.addTagsItem(new Tag().name("Notes").description("Lecture notes written by students and graded by professors"));
	}

	/**
	 * Adds the 401 response to every operation that requires a token, instead of repeating it on each method.
	 * Operations with their own (empty) security requirement are public and are skipped.
	 * @return the customizer
	 */
	@Bean
	OpenApiCustomizer unauthorizedResponseCustomizer() {
		return openApi -> openApi.getPaths()
			.values()
			.forEach(path -> path.readOperations()
				.stream()
				.filter(operation -> operation.getSecurity() == null)
				.forEach(operation -> operation.getResponses()
					.addApiResponse("401", new ApiResponse().description("Missing, invalid or expired bearer token")
						.content(new Content().addMediaType(ApiErrors.PROBLEM_JSON, new MediaType()
							.schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")))))));
	}

	/**
	 * Documents a {@code 403} response on every handler guarded by {@link PreAuthorize}, since the caller's role or
	 * identity may not satisfy the rule.
	 * @return the operation customizer
	 */
	@Bean
	OperationCustomizer forbiddenResponseCustomizer() {
		return (operation, handlerMethod) -> {
			if (AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), PreAuthorize.class)) {
				operation.getResponses()
					.addApiResponse("403", new ApiResponse().description("The caller's role or identity is not allowed")
						.content(new Content().addMediaType(ApiErrors.PROBLEM_JSON, new MediaType()
							.schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")))));
			}
			return operation;
		};
	}

}
