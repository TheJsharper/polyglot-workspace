package com.example.project00.domain;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Notes a {@link Student} writes for a {@link Lecture} they attended (*..1 to both).
 * <p>
 * Notes may be private and can optionally be reviewed and graded by a {@link Professor}.
 */
@Getter
@Entity
@Table(name = "notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notes extends BaseEntity {

	/** Lowest grade a reviewer may give. */
	public static final int MIN_GRADE = 0;

	/** Highest grade a reviewer may give. */
	public static final int MAX_GRADE = 100;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lecture_id", nullable = false)
	private Lecture lecture;

	@Setter
	@Column(nullable = false, length = 10_000)
	private String content;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant timestamp;

	@Setter
	@Column(name = "is_private", nullable = false)
	private boolean isPrivate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private Professor reviewer;

	private Integer grade;

	@Column(name = "review_comment", length = 2_000)
	private String reviewComment;

	/**
	 * Creates notes, timestamped with the current instant.
	 * @param student the author
	 * @param lecture the lecture the notes are about
	 * @param content the note text
	 * @param isPrivate whether only the author may see the notes
	 * @throws NullPointerException if {@code student}, {@code lecture} or {@code content} is {@code null}
	 * @throws IllegalArgumentException if the student did not attend the lecture
	 */
	public Notes(Student student, Lecture lecture, String content, boolean isPrivate) {
		this.student = Objects.requireNonNull(student, "student");
		this.lecture = Objects.requireNonNull(lecture, "lecture");
		if (!lecture.hasAttendee(student)) {
			throw new IllegalArgumentException(
					"Student " + student.getStudentId() + " did not attend lecture '" + lecture.getTopic() + "'");
		}
		this.content = Objects.requireNonNull(content, "content");
		this.isPrivate = isPrivate;
		this.timestamp = Instant.now();
	}

	/**
	 * Reviews and grades the notes; a later review replaces an earlier one.
	 * @param reviewer the reviewing professor
	 * @param grade a grade between {@link #MIN_GRADE} and {@link #MAX_GRADE}
	 * @param comment optional feedback, may be {@code null}
	 * @throws NullPointerException if {@code reviewer} is {@code null}
	 * @throws IllegalArgumentException if the grade is out of range
	 */
	public void review(Professor reviewer, int grade, String comment) {
		Objects.requireNonNull(reviewer, "reviewer");
		if (reviewer.isDemo() != student.isDemo()) {
			throw new IllegalArgumentException("Demo and real data cannot be mixed");
		}
		if (grade < MIN_GRADE || grade > MAX_GRADE) {
			throw new IllegalArgumentException(
					"Grade must be between " + MIN_GRADE + " and " + MAX_GRADE + " but was " + grade);
		}
		this.reviewer = reviewer;
		this.grade = grade;
		this.reviewComment = comment;
	}

	/** @return whether a professor has reviewed these notes */
	public boolean isReviewed() {
		return reviewer != null;
	}

}
