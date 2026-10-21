package com.example.project01.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.project01.service.ResourceNotFoundException;

/** Maps domain exceptions to concise JSON errors. */
@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	ResponseEntity<Map<String, String>> conflict(IllegalStateException ex) {
		return error(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class })
	ResponseEntity<Map<String, String>> badRequest(Exception ex) {
		return error(HttpStatus.BAD_REQUEST, "Invalid request");
	}

	private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("error", message));
	}

}
