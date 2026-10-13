package com.example.project00.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.project00.domain.Lecture;
import com.example.project00.domain.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request and response payloads of the {@code /api/lectures} endpoints.
 */
public final class LectureDtos {

	private LectureDtos() {
	}

	@Schema(description = "Data needed to schedule a lecture")
	public record ScheduleLectureRequest(
			@Schema(description = "Code of the course the lecture belongs to", example = "CS101") @NotBlank String courseCode,
			@Schema(description = "Employee id of the delivering professor", example = "E-1") @NotBlank String employeeId,
			@Schema(description = "Lecture date (ISO-8601)", example = "2026-10-14") @NotNull LocalDate date,
			@Schema(description = "Start time (ISO-8601)", type = "string", format = "time", example = "09:00:00") @NotNull LocalTime time,
			@Schema(description = "Lecture topic", example = "Variables and types") @NotBlank String topic,
			@Schema(description = "Room", example = "A-101", maxLength = 50) @NotBlank @Size(max = 50) String room) {
	}

	@Schema(description = "New date, time and room for an existing lecture")
	public record RescheduleLectureRequest(
			@Schema(description = "Lecture date (ISO-8601)", example = "2026-10-21") @NotNull LocalDate date,
			@Schema(description = "Start time (ISO-8601)", type = "string", format = "time", example = "11:30:00") @NotNull LocalTime time,
			@Schema(description = "Room", example = "B-202", maxLength = 50) @NotBlank @Size(max = 50) String room) {
	}

	@Schema(description = "The professor who should deliver the lecture from now on")
	public record AssignProfessorRequest(
			@Schema(description = "Employee id of the new professor", example = "E-2") @NotBlank String employeeId) {
	}

	@Schema(description = "A scheduled lecture")
	public record LectureResponse(
			@Schema(description = "Database id", example = "1") Long id,
			@Schema(description = "Course code", example = "CS101") String courseCode,
			@Schema(description = "Employee id of the delivering professor", example = "E-1") String employeeId,
			@Schema(description = "Lecture date", example = "2026-10-14") LocalDate date,
			@Schema(description = "Start time", type = "string", format = "time", example = "09:00:00") LocalTime time,
			@Schema(description = "Lecture topic", example = "Variables and types") String topic,
			@Schema(description = "Room", example = "A-101") String room,
			@Schema(description = "Student ids of the attendees, sorted") List<String> attendeeIds) {

		/** Maps a {@link Lecture} entity to its API representation. */
		public static LectureResponse from(Lecture lecture) {
			return new LectureResponse(lecture.getId(), lecture.getCourse().getCourseCode(),
					lecture.getProfessor().getEmployeeId(), lecture.getDate(), lecture.getTime(), lecture.getTopic(),
					lecture.getRoom(), lecture.getAttendees().stream().map(Student::getStudentId).sorted().toList());
		}

	}

}
