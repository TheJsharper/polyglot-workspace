package com.example.project01.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.project01.domain.DemoKey;

/** Executes key-generation jobs on the bounded {@code demoKeyExecutor} pool. */
@Component
public class DemoKeyJobRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoKeyJobRunner.class);

	private final DemoKeyService keys;
	private final DemoKeyJobStore store;

	public DemoKeyJobRunner(DemoKeyService keys, DemoKeyJobStore store) {
		this.keys = keys;
		this.store = store;
	}

	@Async("demoKeyExecutor")
	public void run(String jobId, int count, int days, String label, String createdBy) {
		store.update(jobId, job -> job.withStatus(DemoKeyJob.Status.RUNNING));
		try {
			List<DemoKey> created = keys.create(count, days, label, createdBy);
			store.update(jobId, job -> job.done(created.stream().map(DemoKeyView::of).toList()));
		}
		catch (IllegalArgumentException ex) {
			store.update(jobId, job -> job.failed(ex.getMessage()));
		}
		catch (RuntimeException ex) {
			log.error("Demo key job {} failed", jobId, ex);
			store.update(jobId, job -> job.failed("Key generation failed"));
		}
	}

}
