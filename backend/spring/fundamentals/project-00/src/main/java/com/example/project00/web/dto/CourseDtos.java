package com.example.project00.web.dto;

import java.util.List;

import com.example.project00.domain.Course;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request and response payloads of the {@code /api/courses} endpoints.
 */
public final class CourseDtos {

	private CourseDtos() {
	}

	@Schema(description = "Data needed to create a course")
	public record CreateCourseRequest(
			@Schema(description = "Unique course code", example = "CS101", maxLength = 20) @NotBlank @Size(max = 20) String courseCode,
			@Schema(description = "Course title", example = "Introduction to Programming") @NotBlank String title,
			@Schema(description = "Credit points awarded on completion", example = "6", minimum = "1") @Positive int credits) {
	}

	@Schema(description = "Editable course fields; the course code cannot change")
	public record UpdateCourseRequest(
			@Schema(description = "Course title", example = "Programming I") @NotBlank String title,
			@Schema(description = "Credit points awarded on completion", example = "8", minimum = "1") @Positive int credits) {
	}

	@Schema(description = "A course in the catalogue")
	public record CourseResponse(
			@Schema(description = "Database id", example = "1") Long id,
			@Schema(description = "Unique course code", example = "CS101") String courseCode,
			@Schema(description = "Course title", example = "Introduction to Programming") String title,
			@Schema(description = "Credit points", example = "6") int credits,
			@Schema(description = "Employee ids of the professors teaching the course", example = "[\"E-1\"]") List<String> teacherIds,
			@Schema(description = "Student ids of the enrolled students", example = "[\"S-1\"]") List<String> enrolledStudentIds) {

		/** Maps a {@link Course} entity to its API representation. */
		public static CourseResponse from(Course course) {
			return new CourseResponse(course.getId(), course.getCourseCode(), course.getTitle(), course.getCredits(),
					course.getTeachers().stream().map(Professor::getEmployeeId).sorted().toList(),
					course.getEnrolledStudents().stream().map(Student::getStudentId).sorted().toList());
		}

	}

}
