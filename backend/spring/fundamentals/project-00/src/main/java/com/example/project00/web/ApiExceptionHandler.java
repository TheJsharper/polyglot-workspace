package com.example.project00.web;

import com.example.project00.service.DuplicateResourceException;
import com.example.project00.service.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Translates exceptions into RFC 9457 {@link ProblemDetail} responses ({@code application/problem+json}).
 * <table>
 * <caption>Exception mapping</caption>
 * <tr><th>Exception</th><th>Status</th></tr>
 * <tr><td>bean validation failure</td><td>400 (inherited)</td></tr>
 * <tr><td>{@link IllegalArgumentException}</td><td>400</td></tr>
 * <tr><td>{@link ResourceNotFoundException}</td><td>404</td></tr>
 * <tr><td>{@link DuplicateResourceException}</td><td>409</td></tr>
 * <tr><td>{@link IllegalStateException}</td><td>409</td></tr>
 * <tr><td>{@link DataIntegrityViolationException}</td><td>409</td></tr>
 * <tr><td>{@link AuthenticationException} (missing/invalid token, bad credentials)</td><td>401</td></tr>
 * <tr><td>{@link AccessDeniedException} (role or ownership rule failed)</td><td>403</td></tr>
 * </table>
 * Security filter failures are routed here too by the entry point and access-denied handler in
 * {@code SecurityConfig}.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
	}

	@ExceptionHandler(DuplicateResourceException.class)
	ProblemDetail handleDuplicate(DuplicateResourceException ex) {
		return problem(HttpStatus.CONFLICT, "Duplicate resource", ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	ProblemDetail handleConflict(IllegalStateException ex) {
		return problem(HttpStatus.CONFLICT, "Operation not allowed", ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleIntegrityViolation(DataIntegrityViolationException ex) {
		return problem(HttpStatus.CONFLICT, "Data integrity violation", "The request conflicts with existing data");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail handleBadRequest(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<ProblemDetail> handleUnauthenticated(AuthenticationException ex) {
		String detail = ex instanceof BadCredentialsException || ex instanceof UsernameNotFoundException
				? "Invalid username or password" : "A valid bearer token is required";
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
			.body(problem(HttpStatus.UNAUTHORIZED, "Unauthorized", detail));
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleForbidden(AccessDeniedException ex) {
		return problem(HttpStatus.FORBIDDEN, "Forbidden", "You are not allowed to perform this operation");
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		return problem;
	}

}
