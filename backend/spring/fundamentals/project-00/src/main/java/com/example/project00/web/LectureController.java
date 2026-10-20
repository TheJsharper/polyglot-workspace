package com.example.project00.web;

import java.net.URI;
import java.util.List;

import com.example.project00.service.LectureService;
import com.example.project00.service.CreationQuotaService;
import com.example.project00.domain.CreationResource;
import com.example.project00.web.dto.LectureDtos.AssignProfessorRequest;
import com.example.project00.web.dto.LectureDtos.LectureResponse;
import com.example.project00.web.dto.LectureDtos.RescheduleLectureRequest;
import com.example.project00.web.dto.LectureDtos.ScheduleLectureRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for scheduled lectures and their attendance under {@code /api/lectures}.
 * <p>
 * Every authenticated user may read lectures. Scheduling, rescheduling, cancelling and attendance are reserved for
 * professors who teach the lecture's course; the admin may additionally hand a lecture over to another teacher.
 */
@Tag(name = "Lectures")
@RestController
@Transactional
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
class LectureController {

	private final LectureService lectureService;

	private final CreationQuotaService creationQuota;

	@Operation(summary = "Get a lecture by id")
	@ApiResponse(responseCode = "200", description = "The lecture, including its attendees")
	@ApiErrors.NotFound
	@GetMapping("/{lectureId}")
	LectureResponse get(@Parameter(description = "Lecture id", example = "1") @PathVariable Long lectureId) {
		return LectureResponse.from(lectureService.getById(lectureId));
	}

	@Operation(summary = "List a course's lectures", description = "Sorted by date, then start time.")
	@ApiResponse(responseCode = "200", description = "The course's lectures in chronological order")
	@ApiErrors.NotFound
	@GetMapping(params = "courseCode")
	List<LectureResponse> findByCourse(
			@Parameter(description = "Course code to filter by", example = "CS101") @RequestParam String courseCode) {
		return lectureService.findByCourse(courseCode).stream().map(LectureResponse::from).toList();
	}

	@Operation(summary = "List the lectures a professor delivers")
	@ApiResponse(responseCode = "200", description = "The professor's lectures")
	@ApiErrors.NotFound
	@GetMapping(params = "employeeId")
	List<LectureResponse> findByProfessor(
			@Parameter(description = "Employee id to filter by", example = "E-1") @RequestParam String employeeId) {
		return lectureService.findByProfessor(employeeId).stream().map(LectureResponse::from).toList();
	}

	@Operation(summary = "Schedule a lecture for a course, delivered by a professor",
			description = "Only professors teaching the course. The delivering professor must teach it too (400 otherwise).")
	@ApiResponse(responseCode = "201", description = "Lecture created; the Location header points to it")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.teachesCourse(#request.courseCode())")
	@PostMapping
	ResponseEntity<LectureResponse> schedule(@Valid @RequestBody ScheduleLectureRequest request,
			Authentication authentication) {
		creationQuota.consumeIfGoogleUser(authentication.getName(), CreationResource.LECTURE);
		LectureResponse scheduled = LectureResponse.from(lectureService.schedule(request.courseCode(),
				request.employeeId(), request.date(), request.time(), request.topic(), request.room()));
		return ResponseEntity.created(URI.create("/api/lectures/" + scheduled.id())).body(scheduled);
	}

	@Operation(summary = "Move a lecture to a new date, time and room")
	@ApiResponse(responseCode = "200", description = "The rescheduled lecture")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.teachesLecture(#lectureId)")
	@PutMapping("/{lectureId}/schedule")
	LectureResponse reschedule(@Parameter(description = "Lecture id", example = "1") @PathVariable Long lectureId,
			@Valid @RequestBody RescheduleLectureRequest request) {
		return LectureResponse
			.from(lectureService.reschedule(lectureId, request.date(), request.time(), request.room()));
	}

	@Operation(summary = "Hand a lecture over to another professor",
			description = "Admin, or a professor teaching the course. The new professor must teach the course (400 otherwise).")
	@ApiResponse(responseCode = "200", description = "The lecture with its new professor")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("hasRole('ADMIN') or @authz.teachesLecture(#lectureId)")
	@PutMapping("/{lectureId}/professor")
	LectureResponse assignProfessor(@Parameter(description = "Lecture id", example = "1") @PathVariable Long lectureId,
			@Valid @RequestBody AssignProfessorRequest request) {
		return LectureResponse.from(lectureService.assignProfessor(lectureId, request.employeeId()));
	}

	@Operation(summary = "Record that a student attended a lecture",
			description = "Only professors teaching the course. The student must be enrolled in the course (400 otherwise). Idempotent.")
	@ApiResponse(responseCode = "200", description = "The lecture with the updated attendee list")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.teachesLecture(#lectureId)")
	@PutMapping("/{lectureId}/attendees/{studentId}")
	LectureResponse recordAttendance(@Parameter(description = "Lecture id", example = "1") @PathVariable Long lectureId,
			@Parameter(description = "Student id", example = "S-1") @PathVariable String studentId) {
		return LectureResponse.from(lectureService.recordAttendance(lectureId, studentId));
	}

	@Operation(summary = "Remove a student's attendance from a lecture",
			description = "Only professors teaching the course. Fails with 409 if the student wrote notes for this lecture.")
	@ApiResponse(responseCode = "200", description = "The lecture with the updated attendee list")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("@authz.teachesLecture(#lectureId)")
	@DeleteMapping("/{lectureId}/attendees/{studentId}")
	LectureResponse removeAttendance(@Parameter(description = "Lecture id", example = "1") @PathVariable Long lectureId,
			@Parameter(description = "Student id", example = "S-1") @PathVariable String studentId) {
		return LectureResponse.from(lectureService.removeAttendance(lectureId, studentId));
	}

	@Operation(summary = "Cancel a lecture",
			description = "Only professors teaching the course. Fails with 409 if any notes were written for it.")
	@ApiResponse(responseCode = "204", description = "Lecture cancelled")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("@authz.teachesLecture(#lectureId)")
	@DeleteMapping("/{lectureId}")
	ResponseEntity<Void> cancel(@Parameter(description = "Lecture id", example = "1") @PathVariable Long lectureId) {
		lectureService.cancel(lectureId);
		return ResponseEntity.noContent().build();
	}

}
