package com.example.project00.web;

import java.net.URI;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.project00.service.DemoLoginService;
import com.example.project00.service.DemoLoginService.CreatedDemoLogin;
import com.example.project00.service.DemoLoginService.DemoLogin;
import com.example.project00.service.DemoLoginService.Persona;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/**
 * Lets real admins and the manager generate read-only, expiring recruiter logins. Demo logins themselves can never
 * use these endpoints.
 */
@Tag(name = "Demo logins")
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-logins.enabled", havingValue = "true", matchIfMissing = true)
class DemoLoginController {

	private static final String BASE = "/api/admin/demo-logins";

	private static final String GUARD = "hasAnyRole('ADMIN','MANAGER') and !@demoScope.isDemoCaller()";

	private final DemoLoginService demoLogins;

	record CreateDemoLoginRequest(@NotNull Persona persona,
			@Min(1) @Max(DemoLoginService.MAX_DAYS) Integer days,
			@Size(max = DemoLoginService.MAX_LABEL_LENGTH) String label) {
	}

	record DemoLoginResponse(String username, String password, Persona persona, String label, String createdBy,
			java.time.Instant createdAt, java.time.Instant expiresAt) {
	}

	@Operation(summary = "Lists active demo logins, newest first (passwords are never returned)")
	@PreAuthorize(GUARD)
	@GetMapping(BASE)
	List<DemoLogin> list() {
		return demoLogins.list();
	}

	@Operation(summary = "Generates a demo login; the password is only shown in this response")
	@PreAuthorize(GUARD)
	@PostMapping(BASE)
	ResponseEntity<DemoLoginResponse> create(@Valid @RequestBody CreateDemoLoginRequest request,
			Authentication caller) {
		CreatedDemoLogin created = demoLogins.create(request.persona(), request.label(), request.days(),
				caller.getName());
		DemoLogin login = created.login();
		return ResponseEntity.created(URI.create(BASE + "/" + login.username()))
			.cacheControl(CacheControl.noStore())
			.body(new DemoLoginResponse(login.username(), created.password(), login.persona(), login.label(),
					login.createdBy(), login.createdAt(), login.expiresAt()));
	}

	@Operation(summary = "Revokes a demo login immediately and deletes its persona")
	@PreAuthorize(GUARD)
	@DeleteMapping(BASE + "/{username}")
	ResponseEntity<Void> revoke(@PathVariable String username) {
		demoLogins.revoke(username);
		return ResponseEntity.noContent().build();
	}

}
