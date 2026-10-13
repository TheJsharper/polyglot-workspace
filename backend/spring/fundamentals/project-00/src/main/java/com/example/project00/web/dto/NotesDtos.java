package com.example.project00.web.dto;

import java.time.Instant;

import com.example.project00.domain.Notes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request and response payloads of the {@code /api/notes} endpoints.
 */
public final class NotesDtos {

	private NotesDtos() {
	}

	@Schema(description = "Notes a student writes for a lecture they attended")
	public record WriteNotesRequest(
			@Schema(description = "Author's student id", example = "S-1") @NotBlank String studentId,
			@Schema(description = "Id of the attended lecture", example = "1") @NotNull Long lectureId,
			@Schema(description = "Note text", example = "A variable is a named storage location.") @NotBlank String content,
			@Schema(description = "Whether only the author may see the notes", example = "false") boolean privateNote) {
	}

	@Schema(description = "New content and visibility for existing notes")
	public record EditNotesRequest(
			@Schema(description = "Note text", example = "Variables have a name, a type and a value.") @NotBlank String content,
			@Schema(description = "Whether only the author may see the notes", example = "true") boolean privateNote) {
	}

	@Schema(description = "A professor's review of notes")
	public record ReviewNotesRequest(
			@Schema(description = "Reviewer's employee id", example = "E-1") @NotBlank String employeeId,
			@Schema(description = "Grade from 0 to 100", example = "92", minimum = "0", maximum = "100") @NotNull @Min(Notes.MIN_GRADE) @Max(Notes.MAX_GRADE) Integer grade,
			@Schema(description = "Optional feedback", example = "Clear and concise", nullable = true) String comment) {
	}

	@Schema(description = "Notes written by a student during a lecture")
	public record NotesResponse(
			@Schema(description = "Database id", example = "1") Long id,
			@Schema(description = "Author's student id", example = "S-1") String studentId,
			@Schema(description = "Lecture id", example = "1") Long lectureId,
			@Schema(description = "Note text", example = "A variable is a named storage location.") String content,
			@Schema(description = "Whether only the author may see the notes", example = "false") boolean privateNote,
			@Schema(description = "Creation time (UTC)", example = "2026-10-14T09:45:00Z") Instant timestamp,
			@Schema(description = "Reviewer's employee id; null until reviewed", example = "E-1", nullable = true) String reviewerId,
			@Schema(description = "Grade from 0 to 100; null until reviewed", example = "92", nullable = true) Integer grade,
			@Schema(description = "Reviewer feedback", example = "Clear and concise", nullable = true) String reviewComment) {

		/** Maps a {@link Notes} entity to its API representation. */
		public static NotesResponse from(Notes notes) {
			return new NotesResponse(notes.getId(), notes.getStudent().getStudentId(), notes.getLecture().getId(),
					notes.getContent(), notes.isPrivate(), notes.getTimestamp(),
					notes.getReviewer() == null ? null : notes.getReviewer().getEmployeeId(), notes.getGrade(),
					notes.getReviewComment());
		}

	}

}
