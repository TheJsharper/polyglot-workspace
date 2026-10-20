package com.example.project00.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.project00.domain.Role;
import com.example.project00.service.DemoTokenService;
import com.example.project00.service.DemoTokenService.Redeemed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/** Recruiter demo endpoints; active only in the loopback-bound dev profile. */
@Tag(name = "Demo")
@RestController
@RequiredArgsConstructor
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
class DemoController {

	private final DemoTokenService service;

	record RedeemRequest(@NotBlank @Size(max = 2048) String token, @NotNull Role persona) {
	}

	record RedeemResponse(String accessToken, String tokenType, long expiresIn, String username, String role,
			Instant demoExpiresAt) {
	}

	@Operation(summary = "Reports that local demo access is enabled")
	@SecurityRequirements
	@GetMapping("/api/demo/status")
	Map<String, Boolean> status() {
		return Map.of("enabled", true);
	}

	@Operation(summary = "Redeems a signed token for a read-only development demo session")
	@SecurityRequirements
	@PostMapping("/api/demo/redeem")
	RedeemResponse redeem(@Valid @RequestBody RedeemRequest request, HttpServletRequest http) {
		Redeemed redeemed = service.redeem(request.token(), request.persona(), http.getRemoteAddr());
		return new RedeemResponse(redeemed.accessToken().value(), "Bearer", redeemed.accessToken().expiresInSeconds(),
				redeemed.username(), redeemed.role(), redeemed.expiresAt());
	}

}
