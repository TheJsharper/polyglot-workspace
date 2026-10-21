package com.example.project00.web;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.project00.domain.DemoKey;
import com.example.project00.service.DemoKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/** Manager-only administration of recruiter demo keys; active only in the dev profile. */
@Tag(name = "Demo keys")
@RestController
@RequiredArgsConstructor
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
class DemoKeyController {

	private final DemoKeyService service;

	record CreateDemoKeysRequest(@Min(DemoKeyService.MIN_COUNT) @Max(DemoKeyService.MAX_COUNT) int count,
			@Min(DemoKeyService.MIN_DAYS) @Max(DemoKeyService.MAX_DAYS) Integer days,
			@Size(max = DemoKeyService.MAX_LABEL_LENGTH) String label) {
	}

	/** A demo key as shown to the manager. The private key is never included. */
	record DemoKeyResponse(Long id, String label, String publicKey, String token, Instant createdAt,
			Instant notBefore, Instant expiresAt, boolean redeemed, Instant redeemedAt, int redeemCount,
			String createdBy) {

		static DemoKeyResponse of(DemoKey key) {
			return new DemoKeyResponse(key.getId(), key.getLabel(), key.getPublicKey(), key.getToken(),
					key.getCreatedAt(), key.getNotBefore(), key.getExpiresAt(), key.isRedeemed(),
					key.getRedeemedAt(), key.getRedeemCount(), key.getCreatedBy());
		}
	}

	@Operation(summary = "Lists demo keys, newest first")
	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping("/api/manager/demo-keys")
	List<DemoKeyResponse> list() {
		return service.list().stream().map(DemoKeyResponse::of).toList();
	}

	@Operation(summary = "Creates 1 to 10 demo keys, each with its own keypair and token")
	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping("/api/manager/demo-keys")
	@ResponseStatus(HttpStatus.CREATED)
	List<DemoKeyResponse> create(@Valid @RequestBody CreateDemoKeysRequest request, Authentication caller) {
		int days = request.days() == null ? 10 : request.days();
		return service.create(request.count(), days, request.label(), caller.getName())
			.stream()
			.map(DemoKeyResponse::of)
			.toList();
	}

	@Operation(summary = "Deletes a demo key that has not been redeemed")
	@PreAuthorize("hasRole('MANAGER')")
	@DeleteMapping("/api/manager/demo-keys/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable Long id) {
		service.deleteUnused(id);
	}

}
