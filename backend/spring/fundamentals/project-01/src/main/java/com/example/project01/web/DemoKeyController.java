package com.example.project01.web;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.project01.service.DemoKeyJob;
import com.example.project01.service.DemoKeyJobService;
import com.example.project01.service.DemoKeyService;
import com.example.project01.service.DemoKeyView;
import com.example.project01.service.DemoTokenVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/** Service-to-service API for demo keys. Key generation is asynchronous: accept, then poll the job. */
@RestController
@RequiredArgsConstructor
class DemoKeyController {

	private static final String BASE = "/internal/demo-keys";

	private final DemoKeyJobService jobs;
	private final DemoKeyService keys;
	private final DemoTokenVerifier verifier;

	record CreateRequest(@Min(DemoKeyService.MIN_COUNT) @Max(DemoKeyService.MAX_COUNT) int count,
			@Min(DemoKeyService.MIN_DAYS) @Max(DemoKeyService.MAX_DAYS) Integer days,
			@Size(max = DemoKeyService.MAX_LABEL_LENGTH) String label) {
	}

	record RedeemRequest(@NotBlank @Size(max = 2048) String token) {
	}

	record RedeemResult(boolean valid, String jti, Instant expiresAt) {
	}

	@PostMapping(BASE + "/jobs")
	ResponseEntity<DemoKeyJob> create(@Valid @RequestBody CreateRequest request, Authentication caller) {
		int days = request.days() == null ? 10 : request.days();
		DemoKeyJob job = jobs.submit(request.count(), days, request.label(), caller.getName());
		return ResponseEntity.accepted().location(URI.create(BASE + "/jobs/" + job.id())).body(job);
	}

	@GetMapping(BASE + "/jobs/{id}")
	DemoKeyJob job(@PathVariable String id) {
		return jobs.get(id);
	}

	@GetMapping(BASE)
	List<DemoKeyView> list() {
		return keys.list().stream().map(DemoKeyView::of).toList();
	}

	@DeleteMapping(BASE + "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable Long id) {
		keys.deleteUnused(id);
	}

	/** Verifies a token and, when valid, counts the redemption. */
	@PostMapping(BASE + "/redeem")
	RedeemResult redeem(@Valid @RequestBody RedeemRequest request) {
		JWTClaimsSet claims = verifier.verify(request.token()).orElse(null);
		if (claims == null) {
			return new RedeemResult(false, null, null);
		}
		keys.recordRedemption(claims.getJWTID());
		return new RedeemResult(true, claims.getJWTID(), claims.getExpirationTime().toInstant());
	}

}
