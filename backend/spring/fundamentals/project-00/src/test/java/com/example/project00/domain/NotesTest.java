package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NotesTest {

	private Professor professor;

	private Student student;

	private Lecture lecture;

	@BeforeEach
	void setUp() {
		professor = new Professor("E-1", "Ada", "CS");
		student = new Student("S-1", "Alan", 2025);
		lecture = new Lecture(new Course("CS101", "Programming", 6), professor, LocalDate.of(2026, 10, 14),
				LocalTime.of(9, 0), "Variables", "A-101");
		lecture.addAttendee(student);
	}

	@Test
	void attendingStudentCreatesUnreviewedNotes() {
		Instant before = Instant.now();

		Notes notes = new Notes(student, lecture, "Variables hold values", true);

		assertThat(notes.getId()).isNull();
		assertThat(notes.getStudent()).isSameAs(student);
		assertThat(notes.getLecture()).isSameAs(lecture);
		assertThat(notes.getContent()).isEqualTo("Variables hold values");
		assertThat(notes.isPrivate()).isTrue();
		assertThat(notes.getTimestamp()).isBetween(before, Instant.now());
		assertThat(notes.isReviewed()).isFalse();
		assertThat(notes.getReviewer()).isNull();
		assertThat(notes.getGrade()).isNull();
		assertThat(notes.getReviewComment()).isNull();
	}

	@Test
	void studentWhoDidNotAttendCannotCreateNotes() {
		Student absent = new Student("S-2", "Grace", 2024);

		assertThatThrownBy(() -> new Notes(absent, lecture, "Not my lecture", false))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Student S-2 did not attend lecture 'Variables'");
	}

	@Test
	void constructorRejectsMissingRequiredFields() {
		assertThatThrownBy(() -> new Notes(null, lecture, "c", false)).hasMessage("student");
		assertThatThrownBy(() -> new Notes(student, null, "c", false)).hasMessage("lecture");
		assertThatThrownBy(() -> new Notes(student, lecture, null, false)).hasMessage("content");
	}

	@Test
	void settersUpdateContentAndVisibility() {
		Notes notes = new Notes(student, lecture, "Draft", true);

		notes.setContent("Final");
		notes.setPrivate(false);

		assertThat(notes.getContent()).isEqualTo("Final");
		assertThat(notes.isPrivate()).isFalse();
	}

	@Test
	void reviewRecordsReviewerGradeAndComment() {
		Notes notes = new Notes(student, lecture, "Loops repeat work", false);

		notes.review(professor, 92, "Clear and concise");

		assertThat(notes.isReviewed()).isTrue();
		assertThat(notes.getReviewer()).isSameAs(professor);
		assertThat(notes.getGrade()).isEqualTo(92);
		assertThat(notes.getReviewComment()).isEqualTo("Clear and concise");
	}

	@Test
	void reviewCommentIsOptional() {
		Notes notes = new Notes(student, lecture, "Loops repeat work", false);

		notes.review(professor, 75, null);

		assertThat(notes.getGrade()).isEqualTo(75);
		assertThat(notes.getReviewComment()).isNull();
	}

	@ParameterizedTest
	@ValueSource(ints = { Notes.MIN_GRADE, 50, Notes.MAX_GRADE })
	void reviewAcceptsGradesWithinRange(int grade) {
		Notes notes = new Notes(student, lecture, "Loops repeat work", false);

		notes.review(professor, grade, null);

		assertThat(notes.getGrade()).isEqualTo(grade);
	}

	@ParameterizedTest
	@ValueSource(ints = { Integer.MIN_VALUE, -1, 101, Integer.MAX_VALUE })
	void reviewRejectsGradesOutsideRangeAndLeavesNotesUnchanged(int grade) {
		Notes notes = new Notes(student, lecture, "Loops repeat work", false);

		assertThatThrownBy(() -> notes.review(professor, grade, "comment"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Grade must be between 0 and 100 but was " + grade);
		assertThat(notes.isReviewed()).isFalse();
		assertThat(notes.getGrade()).isNull();
		assertThat(notes.getReviewComment()).isNull();
	}

	@Test
	void reviewRejectsMissingReviewer() {
		Notes notes = new Notes(student, lecture, "Loops repeat work", false);

		assertThatThrownBy(() -> notes.review(null, 80, null)).isInstanceOf(NullPointerException.class)
			.hasMessage("reviewer");
		assertThat(notes.isReviewed()).isFalse();
	}

	@Test
	void reReviewReplacesPreviousReview() {
		Notes notes = new Notes(student, lecture, "Loops repeat work", false);
		Professor secondReviewer = new Professor("E-2", "Grace", "CS");
		notes.review(professor, 60, "Needs detail");

		notes.review(secondReviewer, 85, null);

		assertThat(notes.getReviewer()).isSameAs(secondReviewer);
		assertThat(notes.getGrade()).isEqualTo(85);
		assertThat(notes.getReviewComment()).isNull();
	}

}
