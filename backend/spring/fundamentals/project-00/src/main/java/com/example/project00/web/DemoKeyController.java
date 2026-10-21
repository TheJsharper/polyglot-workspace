package com.example.project00.web;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.project00.service.DemoKeysClient;
import com.example.project00.service.DemoKeysClient.DemoKeyJob;
import com.example.project00.service.DemoKeysClient.DemoKeyView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/**
 * Manager-only administration of recruiter demo keys. The work is done by the demo-keys microservice; these
 * endpoints authorise the manager and relay the calls asynchronously. Active only in the dev profile.
 */
@Tag(name = "Demo keys")
@RestController
@RequiredArgsConstructor
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
class DemoKeyController {

	static final int MIN_COUNT = 1;
	static final int MAX_COUNT = 10;
	static final int MIN_DAYS = 1;
	static final int MAX_DAYS = 30;
	static final int DEFAULT_DAYS = 10;
	static final int MAX_LABEL_LENGTH = 100;

	private static final String BASE = "/api/manager/demo-keys";

	private final DemoKeysClient client;

	record CreateDemoKeysRequest(@Min(MIN_COUNT) @Max(MAX_COUNT) int count, @Min(MIN_DAYS) @Max(MAX_DAYS) Integer days,
			@Size(max = MAX_LABEL_LENGTH) String label) {
	}

	@Operation(summary = "Lists demo keys, newest first")
	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping(BASE)
	CompletableFuture<List<DemoKeyView>> list(Authentication caller) {
		return client.list(caller.getName());
	}

	@Operation(summary = "Starts creating 1 to 10 demo keys; answers 202 with a job to poll")
	@PreAuthorize("hasRole('MANAGER')")
	@PostMapping(BASE)
	CompletableFuture<ResponseEntity<DemoKeyJob>> create(@Valid @RequestBody CreateDemoKeysRequest request,
			Authentication caller) {
		int days = request.days() == null ? DEFAULT_DAYS : request.days();
		return client.createJob(request.count(), days, request.label(), caller.getName())
			.thenApply(job -> ResponseEntity.accepted().location(URI.create(BASE + "/jobs/" + job.id())).body(job));
	}

	@Operation(summary = "Reports the progress of a key-creation job; the keys are included once it is DONE")
	@PreAuthorize("hasRole('MANAGER')")
	@GetMapping(BASE + "/jobs/{id}")
	CompletableFuture<DemoKeyJob> job(
			@PathVariable @Pattern(regexp = "[0-9a-fA-F-]{36}", message = "invalid job id") String id,
			Authentication caller) {
		return client.job(id, caller.getName());
	}

	@Operation(summary = "Deletes a demo key that has not been redeemed")
	@PreAuthorize("hasRole('MANAGER')")
	@DeleteMapping(BASE + "/{id}")
	CompletableFuture<ResponseEntity<Void>> delete(@PathVariable Long id, Authentication caller) {
		return client.delete(id, caller.getName()).thenApply(ignored -> ResponseEntity.noContent().build());
	}

}
