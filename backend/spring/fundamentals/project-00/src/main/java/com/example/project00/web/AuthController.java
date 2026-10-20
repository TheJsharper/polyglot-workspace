package com.example.project00.web;

import java.net.URI;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Role;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.security.TokenService;
import com.example.project00.security.TokenService.IssuedToken;
import com.example.project00.service.AccountService;
import com.example.project00.service.RefreshSessionService;
import com.example.project00.web.dto.AuthDtos.LoginRequest;
import com.example.project00.web.dto.AuthDtos.MeResponse;
import com.example.project00.web.dto.AuthDtos.RegisterRequest;
import com.example.project00.web.dto.AuthDtos.RegisterResponse;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
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

	private final RefreshSessionService refreshSessions;

	@Operation(summary = "Register an account",
			description = "Public for role STUDENT (default) and PROFESSOR. Role ADMIN requires an admin access token. "
					+ "The id becomes the login username.")
	@ApiResponse(responseCode = "201", description = "Account registered")
	@ApiErrors.BadRequest
	@ApiErrors.Unauthorized
	@ApiErrors.Forbidden
	@ApiErrors.Conflict
	@SecurityRequirements
	@PostMapping("/register")
	ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request,
			Authentication caller) {
		Role role = request.effectiveRole();
		return switch (role) {
			case STUDENT -> {
				Student student = accountService.registerStudent(request.id(), request.name(),
						request.enrollmentYear(), request.email(), request.password());
				yield ResponseEntity.created(URI.create("/api/students/" + student.getStudentId()))
					.body(new RegisterResponse(student.getStudentId(), role.name(), student.getName()));
			}
			case PROFESSOR -> {
				Professor professor = accountService.registerProfessor(request.id(), request.name(),
						request.department(), request.email(), request.password());
				yield ResponseEntity.created(URI.create("/api/professors/" + professor.getEmployeeId()))
					.body(new RegisterResponse(professor.getEmployeeId(), role.name(), professor.getName()));
			}
			case ADMIN -> {
				requireAdmin(caller);
				UserAccount admin = accountService.registerAdmin(request.id(), request.password());
				yield ResponseEntity.status(HttpStatus.CREATED)
					.body(new RegisterResponse(admin.getUsername(), role.name(), null));
			}
		};
	}

	private static void requireAdmin(Authentication caller) {
		if (caller == null || caller instanceof AnonymousAuthenticationToken || !caller.isAuthenticated()) {
			throw new InsufficientAuthenticationException("An admin token is required to register an admin");
		}
		if (caller.getAuthorities().stream().noneMatch(a -> Role.ADMIN.authority().equals(a.getAuthority()))) {
			throw new AccessDeniedException("Only an admin can register an admin");
		}
	}

	@Operation(summary = "Log in and obtain an access token",
			description = "Public. Send the returned token as `Authorization: Bearer <accessToken>`.")
	@ApiResponse(responseCode = "200", description = "A signed access token")
	@ApiErrors.BadRequest
	@ApiErrors.Unauthorized
	@SecurityRequirements
	@PostMapping("/login")
	ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager
			.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
		IssuedToken token = tokenService.issue(authentication);
		String refresh = refreshSessions.issue(authentication.getName());
		return ResponseEntity.ok()
			.header("Set-Cookie", refreshCookie(refresh).toString())
			.body(new TokenResponse(token.value(), "Bearer", token.expiresInSeconds()));
	}

	@PostMapping("/refresh")
	ResponseEntity<TokenResponse> refresh(@CookieValue(name = "__Host-university-refresh", required = false) String secret) {
		if (secret == null || secret.isBlank()) {
			throw new BadCredentialsException("The session has expired");
		}
		RefreshSessionService.RefreshedSession refreshed = refreshSessions.refresh(secret);
		if (refreshed == null) {
			throw new BadCredentialsException("The session has expired");
		}
		return ResponseEntity.ok()
			.header("Set-Cookie", refreshCookie(refreshed.refreshSecret()).toString())
			.body(refreshed.access());
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout(@CookieValue(name = "__Host-university-refresh", required = false) String secret) {
		if (secret != null && !secret.isBlank()) {
			refreshSessions.revoke(secret);
		}
		return ResponseEntity.noContent().header("Set-Cookie", clearRefreshCookie().toString()).build();
	}

	private static ResponseCookie refreshCookie(String secret) {
		return ResponseCookie.from("__Host-university-refresh", secret)
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path("/")
			.maxAge(RefreshSessionService.MAX_SESSION_AGE)
			.build();
	}

	private static ResponseCookie clearRefreshCookie() {
		return ResponseCookie.from("__Host-university-refresh", "")
			.httpOnly(true)
			.secure(true)
			.sameSite("Lax")
			.path("/")
			.maxAge(0)
			.build();
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
