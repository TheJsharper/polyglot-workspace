package com.example.project01.service;

import java.time.Instant;
import java.util.List;

/**
 * Snapshot of an asynchronous key-generation job.
 * @param id the job id
 * @param status where the job is in its life cycle
 * @param keys the generated keys once {@link Status#DONE}, otherwise empty
 * @param error a short reason when {@link Status#FAILED}, otherwise null
 * @param createdAt when the job was accepted
 */
public record DemoKeyJob(String id, Status status, List<DemoKeyView> keys, String error, Instant createdAt) {

	public enum Status {
		PENDING, RUNNING, DONE, FAILED
	}

	DemoKeyJob withStatus(Status next) {
		return new DemoKeyJob(id, next, keys, error, createdAt);
	}

	DemoKeyJob done(List<DemoKeyView> result) {
		return new DemoKeyJob(id, Status.DONE, result, null, createdAt);
	}

	DemoKeyJob failed(String reason) {
		return new DemoKeyJob(id, Status.FAILED, List.of(), reason, createdAt);
	}

}
