package com.example.project00.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.project00.domain.Role;
import com.example.project00.service.AccountService;
import com.example.project00.service.RefreshSessionService;
import com.example.project00.web.dto.AuthDtos.SwitchRoleRequest;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Lets the dev manager act as a student, professor, admin or manager. Only the manager account and its own
 * personas may call it, and it does not exist outside the dev profile.
 */
@RestController
@RequiredArgsConstructor
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
class RoleSwitchController {

	private final AccountService accounts;

	private final UserDetailsService users;

	private final RefreshSessionService refreshSessions;

	@Operation(summary = "Switch the active role (dev manager only)")
	@PostMapping("/api/auth/switch-role")
	ResponseEntity<TokenResponse> switchRole(@Valid @RequestBody SwitchRoleRequest request, Authentication caller,
			@CookieValue(name = "__Host-university-refresh", required = false) String currentSecret) {
		Role target = parse(request.role());
		String username = accounts.switchTarget(caller.getName(), target);
		UserDetails user = users.loadUserByUsername(username);
		var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
		TokenResponse access = refreshSessions.issueAccessToken(authentication);
		if (currentSecret != null && !currentSecret.isBlank()) {
			refreshSessions.revoke(currentSecret);
		}
		String refresh = refreshSessions.issue(username);
		return ResponseEntity.ok().header("Set-Cookie", AuthController.refreshCookie(refresh).toString()).body(access);
	}

	private static Role parse(String role) {
		try {
			return Role.valueOf(role.trim().toUpperCase(java.util.Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Unknown role");
		}
	}

}
