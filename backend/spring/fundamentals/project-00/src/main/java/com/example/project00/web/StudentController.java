package com.example.project00.web;

import java.net.URI;
import java.util.List;

import com.example.project00.service.StudentService;
import com.example.project00.web.dto.StudentDtos.CreateStudentRequest;
import com.example.project00.web.dto.StudentDtos.StudentResponse;
import com.example.project00.web.dto.StudentDtos.UpdateStudentRequest;
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
 * REST endpoints for enrolled students under {@code /api/students}.
 * <p>
 * Students usually register themselves through {@code POST /api/auth/register}. Staff (admin and professors) may
 * list and read every student; a student may read and edit only their own record. Deleting is admin only.
 */
@Tag(name = "Students")
@RestController
@Transactional
@RequestMapping("/api/students")
@RequiredArgsConstructor
class StudentController {

	private final StudentService studentService;

	@Operation(summary = "List all students", description = "Admin and professors only.")
	@ApiResponse(responseCode = "200", description = "All students")
	@ApiErrors.Forbidden
	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
	@GetMapping
	List<StudentResponse> findAll() {
		return studentService.findAll().stream().map(StudentResponse::from).toList();
	}

	@Operation(summary = "Get a student by student id", description = "Staff, or the student themselves.")
	@ApiResponse(responseCode = "200", description = "The student")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("@authz.isStaffOrStudent(#studentId)")
	@GetMapping("/{studentId}")
	StudentResponse get(@Parameter(description = "Unique student id", example = "S-1") @PathVariable String studentId) {
		return StudentResponse.from(studentService.getByStudentId(studentId));
	}

	@Operation(summary = "Create a student record without a login account",
			description = "Admin only. Students who need to log in register themselves with POST /api/auth/register.")
	@ApiResponse(responseCode = "201", description = "Student created; the Location header points to them")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	ResponseEntity<StudentResponse> enroll(@Valid @RequestBody CreateStudentRequest request) {
		StudentResponse enrolled = StudentResponse
			.from(studentService.enroll(request.studentId(), request.name(), request.enrollmentYear()));
		return ResponseEntity.created(URI.create("/api/students/" + enrolled.studentId())).body(enrolled);
	}

	@Operation(summary = "Update a student's name and enrollment year", description = "Admin, or the student themselves.")
	@ApiResponse(responseCode = "200", description = "The updated student")
	@ApiErrors.BadRequest
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@PreAuthorize("hasRole('ADMIN') or @authz.isStudent(#studentId)")
	@PutMapping("/{studentId}")
	StudentResponse update(@Parameter(description = "Unique student id", example = "S-1") @PathVariable String studentId,
			@Valid @RequestBody UpdateStudentRequest request) {
		return StudentResponse.from(studentService.update(studentId, request.name(), request.enrollmentYear()));
	}

	@Operation(summary = "Delete a student",
			description = "Admin only. Removes the student's attendance, enrollments and login account first. Fails with 409 if the student has notes.")
	@ApiResponse(responseCode = "204", description = "Student deleted")
	@ApiErrors.Forbidden
	@ApiErrors.NotFound
	@ApiErrors.Conflict
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{studentId}")
	ResponseEntity<Void> delete(@Parameter(description = "Unique student id", example = "S-1") @PathVariable String studentId) {
		studentService.delete(studentId);
		return ResponseEntity.noContent().build();
	}

}
