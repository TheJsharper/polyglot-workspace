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

@Getter
@Entity
@Table(name = "notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notes extends BaseEntity {

	public static final int MIN_GRADE = 0;
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

	public void review(Professor reviewer, int grade, String comment) {
		Objects.requireNonNull(reviewer, "reviewer");
		if (grade < MIN_GRADE || grade > MAX_GRADE) {
			throw new IllegalArgumentException(
					"Grade must be between " + MIN_GRADE + " and " + MAX_GRADE + " but was " + grade);
		}
		this.reviewer = reviewer;
		this.grade = grade;
		this.reviewComment = comment;
	}

	public boolean isReviewed() {
		return reviewer != null;
	}

}
