package com.example.project00.web.dto;

import com.example.project00.domain.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request and response payloads of the {@code /api/auth} endpoints.
 */
public final class AuthDtos {

	private AuthDtos() {
	}

	@Schema(description = "Registration for a STUDENT or PROFESSOR (public) or an ADMIN (admin token required). "
			+ "The id becomes the login username.")
	public record RegisterRequest(
			@Schema(description = "Role to register; defaults to STUDENT", example = "STUDENT") Role role,
			@Schema(description = "Student id, employee id or admin username", example = "S-1", maxLength = 254) @NotBlank @Size(max = 254) String id,
			@Schema(description = "Full name (required for STUDENT and PROFESSOR)", example = "Alan Turing") String name,
			@Schema(description = "Year of enrollment (STUDENT only)", example = "2025", minimum = "1900") @Min(1900) Integer enrollmentYear,
			@Schema(description = "Department (PROFESSOR only)", example = "Computer Science") String department,
			@Schema(description = "Password (8-72 characters)", example = "s3cret-pass", minLength = 8, maxLength = 72,
					accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank @Size(min = 8, max = 72) String password,
			@Schema(description = "Optional email (STUDENT and PROFESSOR); can be used instead of the id to log in",
					example = "alan@example.com", maxLength = 254) @Email @Size(max = 254) String email) {

		public RegisterRequest(Role role, String id, String name, Integer enrollmentYear, String department,
				String password) {
			this(role, id, name, enrollmentYear, department, password, null);
		}

		public static RegisterRequest student(String id, String name, int enrollmentYear, String password) {
			return new RegisterRequest(Role.STUDENT, id, name, enrollmentYear, null, password, null);
		}

		public static RegisterRequest professor(String id, String name, String department, String password) {
			return new RegisterRequest(Role.PROFESSOR, id, name, null, department, password, null);
		}

		public static RegisterRequest admin(String id, String password) {
			return new RegisterRequest(Role.ADMIN, id, null, null, null, password, null);
		}

		/** @return the requested role, STUDENT when omitted */
		@JsonIgnore
		public Role effectiveRole() {
			return role == null ? Role.STUDENT : role;
		}

		@JsonIgnore
		@Schema(hidden = true)
		@AssertTrue(message = "name and enrollmentYear are required for STUDENT; name and department for PROFESSOR")
		public boolean isValidForRole() {
			return switch (effectiveRole()) {
				case STUDENT -> hasText(name) && enrollmentYear != null;
				case PROFESSOR -> hasText(name) && hasText(department);
				case ADMIN -> true;
			};
		}

		private static boolean hasText(String value) {
			return value != null && !value.isBlank();
		}
	}

	@Schema(description = "The newly registered account")
	public record RegisterResponse(
			@Schema(description = "Login username", example = "S-1") String username,
			@Schema(description = "Role", example = "STUDENT", allowableValues = { "STUDENT", "PROFESSOR", "ADMIN" }) String role,
			@Schema(description = "Full name; null for admins", example = "Alan Turing") String name) {
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
