package com.example.project01.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Accepts key-generation requests and lets callers poll their progress. */
@Service
public class DemoKeyJobService {

	private final DemoKeyJobStore store;
	private final DemoKeyJobRunner runner;
	private final Clock clock;

	public DemoKeyJobService(DemoKeyJobStore store, DemoKeyJobRunner runner, Clock clock) {
		this.store = store;
		this.runner = runner;
		this.clock = clock;
	}

	/**
	 * Registers a job and starts it in the background.
	 * @return the accepted job in {@code PENDING} state
	 * @throws ResponseStatusException 503 when the worker queue is full
	 */
	public DemoKeyJob submit(int count, int days, String label, String createdBy) {
		String id = UUID.randomUUID().toString();
		DemoKeyJob job = new DemoKeyJob(id, DemoKeyJob.Status.PENDING, List.of(), null, clock.instant());
		store.add(job);
		try {
			runner.run(id, count, days, label, createdBy);
		}
		catch (TaskRejectedException ex) {
			store.update(id, j -> j.failed("Service is busy"));
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service is busy, try again shortly");
		}
		return job;
	}

	/**
	 * @throws ResourceNotFoundException if the job is unknown or has been evicted
	 */
	public DemoKeyJob get(String id) {
		return store.find(id).orElseThrow(() -> new ResourceNotFoundException("Job", id));
	}

}
