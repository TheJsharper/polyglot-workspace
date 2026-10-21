package com.example.project01.service;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoKeyJobStoreTest {

	private static DemoKeyJob job(String id, DemoKeyJob.Status status) {
		return new DemoKeyJob(id, status, List.of(), null, Instant.EPOCH);
	}

	@Test
	void evictsOldestFinishedJobsButKeepsRunningOnes() {
		DemoKeyJobStore store = new DemoKeyJobStore(2);
		store.add(job("running", DemoKeyJob.Status.RUNNING));
		store.add(job("done-1", DemoKeyJob.Status.DONE));
		store.add(job("done-2", DemoKeyJob.Status.DONE));

		assertThat(store.find("running")).isPresent();
		assertThat(store.find("done-1")).isEmpty();
		assertThat(store.find("done-2")).isPresent();
	}

	@Test
	void updatesKnownJobsOnly() {
		DemoKeyJobStore store = new DemoKeyJobStore(5);
		store.add(job("a", DemoKeyJob.Status.PENDING));

		store.update("a", j -> j.failed("boom"));
		store.update("missing", j -> j.failed("boom"));

		assertThat(store.find("a")).get().extracting(DemoKeyJob::status).isEqualTo(DemoKeyJob.Status.FAILED);
		assertThat(store.find("missing")).isEmpty();
	}

}
