package com.example.project00.web;

import java.net.URI;

import com.example.project00.security.TokenService;
import com.example.project00.security.TokenService.IssuedToken;
import com.example.project00.service.AccountService;
import com.example.project00.web.dto.AuthDtos.LoginRequest;
import com.example.project00.web.dto.AuthDtos.MeResponse;
import com.example.project00.web.dto.AuthDtos.RegisterStudentRequest;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import com.example.project00.web.dto.StudentDtos.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints under {@code /api/auth}: student self-registration, login (token issuing) and the
 * current caller. Professors are not registered here; the admin creates them through {@code POST /api/professors}.
 */
@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

	private final AccountService accountService;

	private final AuthenticationManager authenticationManager;

	private final TokenService tokenService;

	@Operation(summary = "Register as a student",
			description = "Public. Creates the student and a login account whose username is the student id.")
	@ApiResponse(responseCode = "201", description = "Student registered; the Location header points to them")
	@ApiErrors.BadRequest
	@ApiErrors.Conflict
	@SecurityRequirements
	@PostMapping("/register")
	ResponseEntity<StudentResponse> register(@Valid @RequestBody RegisterStudentRequest request) {
		StudentResponse registered = StudentResponse.from(accountService.registerStudent(request.studentId(),
				request.name(), request.enrollmentYear(), request.password()));
		return ResponseEntity.created(URI.create("/api/students/" + registered.studentId())).body(registered);
	}

	@Operation(summary = "Log in and obtain an access token",
			description = "Public. Send the returned token as `Authorization: Bearer <accessToken>`.")
	@ApiResponse(responseCode = "200", description = "A signed access token")
	@ApiErrors.BadRequest
	@ApiErrors.Unauthorized
	@SecurityRequirements
	@PostMapping("/login")
	TokenResponse login(@Valid @RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager
			.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
		IssuedToken token = tokenService.issue(authentication);
		return new TokenResponse(token.value(), "Bearer", token.expiresInSeconds());
	}

	@Operation(summary = "Show the authenticated caller")
	@ApiResponse(responseCode = "200", description = "The caller's username and role")
	@GetMapping("/me")
	MeResponse me(Authentication authentication) {
		String role = authentication.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.filter(authority -> authority != null && authority.startsWith("ROLE_"))
			.map(authority -> authority.substring("ROLE_".length()))
			.findFirst()
			.orElse("");
		return new MeResponse(authentication.getName(), role);
	}

}
