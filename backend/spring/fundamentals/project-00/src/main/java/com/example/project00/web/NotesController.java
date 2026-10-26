package com.example.project00.web;

import java.net.URI;
import java.util.List;

import com.example.project00.security.AuthorizationRules;
import com.example.project00.service.NotesService;
import com.example.project00.service.CreationQuotaService;
import com.example.project00.domain.CreationResource;
import com.example.project00.web.dto.NotesDtos.EditNotesRequest;
import com.example.project00.web.dto.NotesDtos.NotesResponse;
import com.example.project00.web.dto.NotesDtos.ReviewNotesRequest;
import com.example.project00.web.dto.NotesDtos.WriteNotesRequest;
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
 * REST endpoints for lecture notes and their reviews under {@code /api/notes}.
 * <p>
 * Students write, edit and delete only their own notes. Professors review notes of the courses they teach.
 * Public notes are readable by every authenticated user; private notes only by their author and the course's
 * teachers. List endpoints silently omit notes the caller may not read.
 */
@Tag(name = "Notes")
@RestController
@Transactional
@RequestMapping("/api/notes")
@RequiredArgsConstructor
class NotesController {

	private final NotesService notesService;

	private final AuthorizationRules authz;

	private final CreationQuotaService creationQuota;

	@Operation(summary = "Get notes by id", description = "Private notes: only the author and the course's teachers.")
	@ApiResponse(responseCode = "200", description = "The notes")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.canReadNotes(#notesId)")
	@GetMapping("/{notesId}")
	NotesResponse get(@Parameter(description = "Notes id", example = "1") @PathVariable Long notesId) {
		return NotesResponse.from(notesService.getById(notesId));
	}

	@Operation(summary = "List the notes written for a lecture")
	@ApiResponse(responseCode = "200", description = "The lecture's notes")
	@ApiErrors.NotFound
	@GetMapping(params = "lectureId")
	List<NotesResponse> findByLecture(
			@Parameter(description = "Lecture id to filter by", example = "1") @RequestParam Long lectureId) {
		return notesService.findByLecture(lectureId).stream().filter(authz::canRead).map(NotesResponse::from).toList();
	}

	@Operation(summary = "List the notes a student has written")
	@ApiResponse(responseCode = "200", description = "The student's notes")
	@ApiErrors.NotFound
	@GetMapping(params = "studentId")
	List<NotesResponse> findByStudent(
			@Parameter(description = "Student id to filter by", example = "S-1") @RequestParam String studentId) {
		return notesService.findByStudent(studentId).stream().filter(authz::canRead).map(NotesResponse::from).toList();
	}

	@Operation(summary = "List unreviewed notes of the courses the caller teaches", description = "Admins see all; professors see their courses.")
	@ApiResponse(responseCode = "200", description = "Unreviewed notes")
	@ApiErrors.Forbidden
	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
	@GetMapping("/pending-review")
	List<NotesResponse> findPendingReview() {
		return notesService.findPendingReview()
			.stream()
			.filter(authz::callerTeaches)
			.map(NotesResponse::from)
			.toList();
	}

	@Operation(summary = "Write notes for a lecture",
			description = "Admins, or professors teaching the lecture's course, write notes on behalf of a student. The student must have attended the lecture, otherwise the request fails with 400.")
	@ApiResponse(responseCode = "201", description = "Notes created; the Location header points to them")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.managesLecture(#request.lectureId())")
	@PostMapping
	ResponseEntity<NotesResponse> write(@Valid @RequestBody WriteNotesRequest request, Authentication authentication) {
		creationQuota.consumeIfGoogleUser(authentication.getName(), CreationResource.NOTE);
		NotesResponse written = NotesResponse.from(notesService.write(request.studentId(), request.lectureId(),
				request.content(), request.privateNote()));
		return ResponseEntity.created(URI.create("/api/notes/" + written.id())).body(written);
	}

	@Operation(summary = "Edit the content and visibility of notes", description = "Admins or professors teaching the lecture's course.")
	@ApiResponse(responseCode = "200", description = "The edited notes")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.managesNotes(#notesId)")
	@PutMapping("/{notesId}")
	NotesResponse edit(@Parameter(description = "Notes id", example = "1") @PathVariable Long notesId,
			@Valid @RequestBody EditNotesRequest request) {
		return NotesResponse.from(notesService.edit(notesId, request.content(), request.privateNote()));
	}

	@Operation(summary = "Review and grade notes",
			description = "Admins, or professors teaching the lecture's course reviewing as themselves. Grade must be between 0 and 100. A new review replaces the previous one, including its comment.")
	@ApiResponse(responseCode = "200", description = "The reviewed notes")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("hasRole('ADMIN') or @authz.isProfessor(#request.employeeId()) and @authz.reviewsNotes(#notesId)")
	@PutMapping("/{notesId}/review")
	NotesResponse review(@Parameter(description = "Notes id", example = "1") @PathVariable Long notesId,
			@Valid @RequestBody ReviewNotesRequest request) {
		return NotesResponse
			.from(notesService.review(notesId, request.employeeId(), request.grade(), request.comment()));
	}

	@Operation(summary = "Delete notes", description = "Admins or professors teaching the lecture's course.")
	@ApiResponse(responseCode = "204", description = "Notes deleted")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.managesNotes(#notesId)")
	@DeleteMapping("/{notesId}")
	ResponseEntity<Void> delete(@Parameter(description = "Notes id", example = "1") @PathVariable Long notesId) {
		notesService.delete(notesId);
		return ResponseEntity.noContent().build();
	}

}
