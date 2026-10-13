package com.example.project00.web;

import java.net.URI;
import java.util.List;

import com.example.project00.service.AccountService;
import com.example.project00.service.ProfessorService;
import com.example.project00.web.dto.ProfessorDtos.CreateProfessorRequest;
import com.example.project00.web.dto.ProfessorDtos.ProfessorResponse;
import com.example.project00.web.dto.ProfessorDtos.UpdateProfessorRequest;
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
 * REST endpoints for teaching staff under {@code /api/professors}.
 * <p>
 * Every authenticated user may read professors; only the admin hires, edits and removes them.
 */
@Tag(name = "Professors")
@RestController
@Transactional
@RequestMapping("/api/professors")
@RequiredArgsConstructor
class ProfessorController {

	private final ProfessorService professorService;

	private final AccountService accountService;

	@Operation(summary = "List all professors")
	@ApiResponse(responseCode = "200", description = "All professors")
	@GetMapping
	List<ProfessorResponse> findAll() {
		return professorService.findAll().stream().map(ProfessorResponse::from).toList();
	}

	@Operation(summary = "Get a professor by employee id")
	@ApiResponse(responseCode = "200", description = "The professor")
	@ApiErrors.NotFound
	@GetMapping("/{employeeId}")
	ProfessorResponse get(@Parameter(description = "Unique employee id", example = "E-1") @PathVariable String employeeId) {
		return ProfessorResponse.from(professorService.getByEmployeeId(employeeId));
	}

	@Operation(summary = "Hire a professor",
			description = "Admin only. Also creates the professor's login account; the username is the employee id.")
	@ApiResponse(responseCode = "201", description = "Professor created; the Location header points to them")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	ResponseEntity<ProfessorResponse> hire(@Valid @RequestBody CreateProfessorRequest request) {
		ProfessorResponse hired = ProfessorResponse
			.from(accountService.registerProfessor(request.employeeId(), request.name(),
					request.department(), request.password()));
		return ResponseEntity.created(URI.create("/api/professors/" + hired.employeeId())).body(hired);
	}

	@Operation(summary = "Update a professor's name and department")
	@ApiResponse(responseCode = "200", description = "The updated professor")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{employeeId}")
	ProfessorResponse update(@Parameter(description = "Unique employee id", example = "E-1") @PathVariable String employeeId,
			@Valid @RequestBody UpdateProfessorRequest request) {
		return ProfessorResponse.from(professorService.update(employeeId, request.name(), request.department()));
	}

	@Operation(summary = "Delete a professor",
			description = "Fails with 409 while the professor still delivers lectures; reassign or cancel them first.")
	@ApiResponse(responseCode = "204", description = "Professor deleted")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{employeeId}")
	ResponseEntity<Void> delete(@Parameter(description = "Unique employee id", example = "E-1") @PathVariable String employeeId) {
		professorService.delete(employeeId);
		return ResponseEntity.noContent().build();
	}

}
