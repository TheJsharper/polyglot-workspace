package com.example.project00.security;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** Small in-memory sliding-window rate limiter keyed by an arbitrary string (IP address, username). */
public class SlidingWindowLimiter {

	private final int maxEvents;

	private final Duration window;

	private final Clock clock;

	private final ConcurrentHashMap<String, Deque<Long>> events = new ConcurrentHashMap<>();

	public SlidingWindowLimiter(int maxEvents, Duration window, Clock clock) {
		this.maxEvents = maxEvents;
		this.window = window;
		this.clock = clock;
	}

	/** @return whether {@code key} has already used up its allowance in the current window */
	public boolean isBlocked(String key) {
		Deque<Long> deque = events.get(key);
		if (deque == null) {
			return false;
		}
		synchronized (deque) {
			evict(deque);
			return deque.size() >= maxEvents;
		}
	}

	/** Records an event; returns {@code false} when it exceeds the allowance (the event is not counted). */
	public boolean tryAcquire(String key) {
		Deque<Long> deque = events.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (deque) {
			evict(deque);
			if (deque.size() >= maxEvents) {
				return false;
			}
			deque.addLast(clock.millis());
			return true;
		}
	}

	/** Records an event unconditionally (e.g. a failed attempt). */
	public void record(String key) {
		Deque<Long> deque = events.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (deque) {
			evict(deque);
			deque.addLast(clock.millis());
		}
		if (events.size() > 10_000) {
			events.entrySet().removeIf(entry -> {
				synchronized (entry.getValue()) {
					evict(entry.getValue());
					return entry.getValue().isEmpty();
				}
			});
		}
	}

	/** Forgets all recorded events. */
	public void clear() {
		events.clear();
	}

	private void evict(Deque<Long> deque) {
		long cutoff = clock.millis() - window.toMillis();
		while (!deque.isEmpty() && deque.peekFirst() <= cutoff) {
			deque.pollFirst();
		}
	}

}
