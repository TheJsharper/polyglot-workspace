package com.example.project00.web.dto;

import com.example.project00.domain.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request and response payloads of the {@code /api/students} endpoints.
 */
public final class StudentDtos {

	private StudentDtos() {
	}

	@Schema(description = "Data needed to enroll a student")
	public record CreateStudentRequest(
			@Schema(description = "Unique student id", example = "S-1", maxLength = 20) @NotBlank @Size(max = 20) String studentId,
			@Schema(description = "Full name", example = "Alan Turing") @NotBlank String name,
			@Schema(description = "Year of enrollment", example = "2025", minimum = "1900") @Min(1900) int enrollmentYear) {
	}

	@Schema(description = "Editable student fields; the student id cannot change")
	public record UpdateStudentRequest(
			@Schema(description = "Full name", example = "Alan Turing") @NotBlank String name,
			@Schema(description = "Year of enrollment", example = "2024", minimum = "1900") @Min(1900) int enrollmentYear) {
	}

	@Schema(description = "An enrolled student")
	public record StudentResponse(
			@Schema(description = "Database id", example = "1") Long id,
			@Schema(description = "Unique student id", example = "S-1") String studentId,
			@Schema(description = "Full name", example = "Alan Turing") String name,
			@Schema(description = "Year of enrollment", example = "2025") int enrollmentYear) {

		/** Maps a {@link Student} entity to its API representation. */
		public static StudentResponse from(Student student) {
			return new StudentResponse(student.getId(), student.getStudentId(), student.getName(),
					student.getEnrollmentYear());
		}

	}

}
