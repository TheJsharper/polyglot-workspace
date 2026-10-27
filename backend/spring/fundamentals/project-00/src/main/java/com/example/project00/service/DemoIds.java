package com.example.project00.service;

import java.util.Locale;

/** Identifier prefix reserved for the generated recruiter demo dataset and demo logins. */
public final class DemoIds {

	/** Prefix of demo usernames, student ids, employee ids and (upper-cased) course codes. */
	public static final String PREFIX = "demo-";

	private DemoIds() {
	}

	/** @return whether the identifier uses the reserved demo prefix (case-insensitive) */
	public static boolean isReserved(String id) {
		return id != null && id.trim().toLowerCase(Locale.ROOT).startsWith(PREFIX);
	}

	/** @throws IllegalArgumentException if the identifier uses the reserved demo prefix */
	public static void requireNotReserved(String id) {
		if (isReserved(id)) {
			throw new IllegalArgumentException("Identifiers starting with '" + PREFIX + "' are reserved for demo data");
		}
	}

}
