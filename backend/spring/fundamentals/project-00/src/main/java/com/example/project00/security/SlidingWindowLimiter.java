package com.example.project00.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A process-local sliding-window limiter for the local demo environment. */
public class SlidingWindowLimiter {

	private final int maxEvents;
	private final Duration window;
	private final Clock clock;
	private final Map<String, Deque<Instant>> events = new ConcurrentHashMap<>();

	public SlidingWindowLimiter(int maxEvents, Duration window, Clock clock) {
		if (maxEvents < 1 || window.isNegative() || window.isZero()) {
			throw new IllegalArgumentException("maxEvents and window must be positive");
		}
		this.maxEvents = maxEvents;
		this.window = window;
		this.clock = clock;
	}

	public synchronized boolean tryAcquire(String key) {
		Deque<Instant> timestamps = events.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		Instant threshold = clock.instant().minus(window);
		while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(threshold)) {
			timestamps.removeFirst();
		}
		if (timestamps.size() >= maxEvents) {
			return false;
		}
		timestamps.addLast(clock.instant());
		return true;
	}

	public synchronized boolean isBlocked(String key) {
		Deque<Instant> timestamps = events.get(key);
		if (timestamps == null) {
			return false;
		}
		Instant threshold = clock.instant().minus(window);
		while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(threshold)) {
			timestamps.removeFirst();
		}
		return timestamps.size() >= maxEvents;
	}

	public synchronized void record(String key) {
		Deque<Instant> timestamps = events.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		timestamps.addLast(clock.instant());
	}

	public synchronized void clear() {
		events.clear();
	}
}
