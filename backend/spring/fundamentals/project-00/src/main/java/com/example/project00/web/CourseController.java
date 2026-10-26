package com.example.project00.web;

import java.net.URI;
import java.util.List;

import com.example.project00.service.CourseService;
import com.example.project00.web.dto.CourseDtos.CourseResponse;
import com.example.project00.web.dto.CourseDtos.CreateCourseRequest;
import com.example.project00.web.dto.CourseDtos.UpdateCourseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for the course catalogue under {@code /api/courses}.
 * <p>
 * Runs in a transaction because open-in-view is disabled: responses are mapped while lazy associations can still
 * be loaded.
 * <p>
 * Every authenticated user may read courses. Only the admin manages the catalogue and assigns teachers; students
 * enroll and unenroll themselves.
 */
@Tag(name = "Courses")
@RestController
@Transactional
@RequestMapping("/api/courses")
@RequiredArgsConstructor
class CourseController {

	private final CourseService courseService;

	@Operation(summary = "List all courses")
	@ApiResponse(responseCode = "200", description = "All courses")
	@GetMapping
	List<CourseResponse> findAll() {
		return courseService.findAll().stream().map(CourseResponse::from).toList();
	}

	@Operation(summary = "Get a course by its code")
	@ApiResponse(responseCode = "200", description = "The course")
	@ApiErrors.NotFound
	@GetMapping("/{courseCode}")
	CourseResponse get(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode) {
		return CourseResponse.from(courseService.getByCode(courseCode));
	}

	@Operation(summary = "Create a course")
	@ApiResponse(responseCode = "201", description = "Course created; the Location header points to it")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	ResponseEntity<CourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
		CourseResponse created = CourseResponse
			.from(courseService.create(request.courseCode(), request.title(), request.credits()));
		return ResponseEntity.created(URI.create("/api/courses/" + created.courseCode())).body(created);
	}

	@Operation(summary = "Update a course's title and credits")
	@ApiResponse(responseCode = "200", description = "The updated course")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.managesCourse(#courseCode)")
	@PutMapping("/{courseCode}")
	CourseResponse update(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode,
			@Valid @RequestBody UpdateCourseRequest request) {
		return CourseResponse.from(courseService.update(courseCode, request.title(), request.credits()));
	}

	@Operation(summary = "Delete a course",
			description = "Also deletes the course's lectures. Fails with 409 if any of those lectures has notes.")
	@ApiResponse(responseCode = "204", description = "Course deleted")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{courseCode}")
	ResponseEntity<Void> delete(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode) {
		courseService.delete(courseCode);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Enroll a student in a course",
			description = "Students may only enroll themselves. Idempotent: enrolling twice has no further effect.")
	@ApiResponse(responseCode = "200", description = "The course with its updated enrollment")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.isStudent(#studentId)")
	@PutMapping("/{courseCode}/students/{studentId}")
	CourseResponse enroll(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode,
			@Parameter(description = "Unique student id", example = "S-1") @PathVariable String studentId) {
		return CourseResponse.from(courseService.enroll(courseCode, studentId));
	}

	@Operation(summary = "Unenroll a student from a course",
			description = "Students may only unenroll themselves. Fails with 409 while they still attend one of the course's lectures.")
	@ApiResponse(responseCode = "200", description = "The course with its updated enrollment")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("@authz.isStudent(#studentId)")
	@DeleteMapping("/{courseCode}/students/{studentId}")
	CourseResponse unenroll(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode,
			@Parameter(description = "Unique student id", example = "S-1") @PathVariable String studentId) {
		return CourseResponse.from(courseService.unenroll(courseCode, studentId));
	}

	@Operation(summary = "Assign a professor to teach a course", description = "Admin only. Idempotent.")
	@ApiResponse(responseCode = "200", description = "The course with its updated teachers")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{courseCode}/professors/{employeeId}")
	CourseResponse assignTeacher(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode,
			@Parameter(description = "Unique employee id", example = "E-1") @PathVariable String employeeId) {
		return CourseResponse.from(courseService.assignTeacher(courseCode, employeeId));
	}

	@Operation(summary = "Remove a professor from a course's teachers",
			description = "Admin only. Fails with 409 while the professor still delivers one of the course's lectures.")
	@ApiResponse(responseCode = "200", description = "The course with its updated teachers")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{courseCode}/professors/{employeeId}")
	CourseResponse unassignTeacher(@Parameter(description = "Unique course code", example = "CS101") @PathVariable String courseCode,
			@Parameter(description = "Unique employee id", example = "E-1") @PathVariable String employeeId) {
		return CourseResponse.from(courseService.unassignTeacher(courseCode, employeeId));
	}

}
