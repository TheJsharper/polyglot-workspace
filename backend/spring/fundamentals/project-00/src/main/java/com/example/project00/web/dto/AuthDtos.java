package com.example.project00.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request and response payloads of the {@code /api/auth} endpoints.
 */
public final class AuthDtos {

	private AuthDtos() {
	}

	@Schema(description = "Student self-registration; the student id becomes the login username")
	public record RegisterStudentRequest(
			@Schema(description = "Unique student id", example = "S-1", maxLength = 20) @NotBlank @Size(max = 20) String studentId,
			@Schema(description = "Full name", example = "Alan Turing") @NotBlank String name,
			@Schema(description = "Year of enrollment", example = "2025", minimum = "1900") @Min(1900) int enrollmentYear,
			@Schema(description = "Password (8-72 characters)", example = "s3cret-pass", minLength = 8, maxLength = 72,
					accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank @Size(min = 8, max = 72) String password) {
	}

	@Schema(description = "Login credentials: a student id, an employee id, or the admin username")
	public record LoginRequest(
			@Schema(description = "Username", example = "S-1") @NotBlank String username,
			@Schema(description = "Password", example = "s3cret-pass", accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank String password) {
	}

	@Schema(description = "A signed access token to send as `Authorization: Bearer <accessToken>`")
	public record TokenResponse(
			@Schema(description = "The JWT", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
			@Schema(description = "Token type", example = "Bearer") String tokenType,
			@Schema(description = "Lifetime in seconds", example = "3600") long expiresIn) {
	}

	@Schema(description = "The authenticated caller")
	public record MeResponse(
			@Schema(description = "Username (student id, employee id or admin name)", example = "S-1") String username,
			@Schema(description = "Role", example = "STUDENT", allowableValues = { "STUDENT", "PROFESSOR", "ADMIN" }) String role) {
	}

}
