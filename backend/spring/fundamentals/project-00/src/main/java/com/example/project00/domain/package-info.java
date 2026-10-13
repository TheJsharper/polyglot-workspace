/**
 * Domain layer: JPA entities of the university model.
 * <p>
 * {@link com.example.project00.domain.Course} 1..* {@link com.example.project00.domain.Lecture};
 * {@link com.example.project00.domain.Professor} 1..* Lecture; {@link com.example.project00.domain.Student} *..*
 * Lecture; {@link com.example.project00.domain.Notes} *..1 Student and *..1 Lecture, optionally reviewed by a
 * Professor. Course *..* Student (enrollment) and Course *..* Professor (teaching) gate who may attend and
 * deliver lectures. {@link com.example.project00.domain.UserAccount} holds login credentials with a
 * {@link com.example.project00.domain.Role}.
 * <p>
 * Entities guard their own invariants (required fields, attendance before notes, grade range) and keep both
 * sides of bidirectional associations in sync. Collections are exposed as read-only views.
 */
package com.example.project00.domain;
