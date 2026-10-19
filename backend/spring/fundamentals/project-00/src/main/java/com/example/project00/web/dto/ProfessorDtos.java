package com.example.project00.web.dto;

import com.example.project00.domain.Professor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request and response payloads of the {@code /api/professors} endpoints.
 */
public final class ProfessorDtos {

	private ProfessorDtos() {
	}

	@Schema(description = "Data needed to hire a professor and create their login account (username = employee id)")
	public record CreateProfessorRequest(
			@Schema(description = "Unique employee id", example = "E-1", maxLength = 254) @NotBlank @Size(max = 254) String employeeId,
			@Schema(description = "Full name", example = "Ada Lovelace") @NotBlank String name,
			@Schema(description = "Department", example = "Computer Science") @NotBlank String department,
			@Schema(description = "Initial password (8-72 characters)", example = "s3cret-pass", minLength = 8, maxLength = 72,
					accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank @Size(min = 8, max = 72) String password) {
	}

	@Schema(description = "Editable professor fields; the employee id cannot change")
	public record UpdateProfessorRequest(
			@Schema(description = "Full name", example = "Ada Lovelace") @NotBlank String name,
			@Schema(description = "Department", example = "Mathematics") @NotBlank String department) {
	}

	@Schema(description = "A member of the teaching staff")
	public record ProfessorResponse(
			@Schema(description = "Database id", example = "1") Long id,
			@Schema(description = "Unique employee id", example = "E-1") String employeeId,
			@Schema(description = "Full name", example = "Ada Lovelace") String name,
			@Schema(description = "Department", example = "Computer Science") String department) {

		/** Maps a {@link Professor} entity to its API representation. */
		public static ProfessorResponse from(Professor professor) {
			return new ProfessorResponse(professor.getId(), professor.getEmployeeId(), professor.getName(),
					professor.getDepartment());
		}

	}

}
