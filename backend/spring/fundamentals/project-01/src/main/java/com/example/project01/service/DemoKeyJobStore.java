package com.example.project01.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** In-memory, bounded store of key-generation jobs. The oldest finished jobs are dropped first. */
@Component
public class DemoKeyJobStore {

	private final Map<String, DemoKeyJob> jobs = new LinkedHashMap<>();
	private final int maxRetained;

	public DemoKeyJobStore(@Value("${app.jobs.max-retained:200}") int maxRetained) {
		this.maxRetained = maxRetained;
	}

	synchronized void add(DemoKeyJob job) {
		jobs.put(job.id(), job);
		Iterator<DemoKeyJob> oldest = jobs.values().iterator();
		while (jobs.size() > maxRetained && oldest.hasNext()) {
			DemoKeyJob candidate = oldest.next();
			if (candidate.status() == DemoKeyJob.Status.DONE || candidate.status() == DemoKeyJob.Status.FAILED) {
				oldest.remove();
			}
		}
	}

	synchronized void update(String id, UnaryOperator<DemoKeyJob> change) {
		DemoKeyJob current = jobs.get(id);
		if (current != null) {
			jobs.put(id, change.apply(current));
		}
	}

	public synchronized Optional<DemoKeyJob> find(String id) {
		return Optional.ofNullable(jobs.get(id));
	}

}
