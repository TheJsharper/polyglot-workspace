package com.example.project00.domain;

/**
 * Roles a caller can have. The admin ("superior") is not stored in the database; students and professors own a
 * {@link UserAccount}.
 */
public enum Role {

	/** Self-registered student: enrolls in courses and writes notes. */
	STUDENT,

	/** Professor registered by an admin: teaches courses, schedules lectures and reviews notes. */
	PROFESSOR,

	/** Superior role, defined in configuration: manages courses, professors and teaching assignments. */
	ADMIN;

	/** @return the Spring Security authority for this role, e.g. {@code ROLE_STUDENT} */
	public String authority() {
		return "ROLE_" + name();
	}

}
