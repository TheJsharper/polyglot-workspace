package com.example.project00;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal greeting endpoint used as a smoke test; not part of the documented {@code /api/**} surface.
 */
@RestController
class HelloController {

	@SecurityRequirements
	@GetMapping("/hello")
	String hello() {
		return "Hello from Spring Boot!";
	}
}
