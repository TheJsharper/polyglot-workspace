package com.example.project00.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.project00.web.dto.NotesDtos.EditNotesRequest;
import com.example.project00.web.dto.NotesDtos.NotesResponse;
import com.example.project00.web.dto.NotesDtos.ReviewNotesRequest;
import com.example.project00.web.dto.NotesDtos.WriteNotesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotesApiE2ETest extends EndToEndTestSupport {

	private Long lectureId;

	@BeforeEach
	void createReferenceData() {
		createCourse("CS101");
		hireProfessor("E-1");
		enrollStudent("S-1");
		enrollStudent("S-2");
		lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");
		recordAttendance(lectureId, "S-2");
	}

	@Test
	void writeReturnsCreatedNotes() {
		NotesResponse notes = postJson(as("E-1"), "/api/notes", new WriteNotesRequest("S-1", lectureId, "Variables hold values", true))
			.expectStatus()
			.isCreated()
			.expectBody(NotesResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(notes.id()).isNotNull();
		assertThat(notes.studentId()).isEqualTo("S-1");
		assertThat(notes.lectureId()).isEqualTo(lectureId);
		assertThat(notes.content()).isEqualTo("Variables hold values");
		assertThat(notes.privateNote()).isTrue();
		assertThat(notes.timestamp()).isNotNull();
		assertThat(notes.reviewerId()).isNull();
		assertThat(notes.grade()).isNull();
	}

	@Test
	void writeByStudentWhoDidNotAttendReturnsBadRequest() {
		enrollStudent("S-3");

		postJson(as("E-1"), "/api/notes", new WriteNotesRequest("S-3", lectureId, "Notes", false)).expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Student S-3 did not attend lecture 'Variables'");
	}

	@Test
	void writeForUnknownLectureReturnsNotFound() {
		postJson(as("E-1"), "/api/notes", new WriteNotesRequest("S-1", 999999L, "Notes", false)).expectStatus().isNotFound();
	}

	@Test
	void getReturnsNotes() {
		Long notesId = writeNotes("S-1", lectureId, "Variables hold values").id();

		client.get().uri("/api/notes/{id}", notesId).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.content").isEqualTo("Variables hold values");
	}

	@Test
	void findByLectureAndByStudentFilterNotes() {
		writeNotes("S-1", lectureId, "Alan's notes");
		writeNotes("S-2", lectureId, "Grace's notes");

		client.get().uri("/api/notes?lectureId={id}", lectureId).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(2);
		client.get().uri("/api/notes?studentId=S-2").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].content").isEqualTo("Grace's notes");
	}

	@Test
	void editChangesContentAndVisibility() {
		Long notesId = writeNotes("S-1", lectureId, "Draft").id();

		putJson(as("S-1"), "/api/notes/{id}", new EditNotesRequest("Hijacked", false), notesId).expectStatus()
			.isForbidden();

		putJson(as("E-1"), "/api/notes/{id}", new EditNotesRequest("Final", true), notesId).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.content").isEqualTo("Final")
			.jsonPath("$.privateNote").isEqualTo(true);
	}

	@Test
	void reviewRecordsGradeAndRemovesNotesFromPendingList() {
		Long reviewedId = writeNotes("S-1", lectureId, "Reviewed").id();
		writeNotes("S-2", lectureId, "Pending");

		putJson(as("E-1"), "/api/notes/{id}/review", new ReviewNotesRequest("E-1", 92, "Clear and concise"), reviewedId)
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.reviewerId").isEqualTo("E-1")
			.jsonPath("$.grade").isEqualTo(92)
			.jsonPath("$.reviewComment").isEqualTo("Clear and concise");

		as("S-1").get().uri("/api/notes/pending-review").exchange().expectStatus().isForbidden();
		as("E-1").get().uri("/api/notes/pending-review").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].content").isEqualTo("Pending");
	}

	@Test
	void reviewWithOutOfRangeGradeReturnsBadRequest() {
		Long notesId = writeNotes("S-1", lectureId, "Notes").id();

		putJson(as("E-1"), "/api/notes/{id}/review", new ReviewNotesRequest("E-1", 101, null), notesId).expectStatus()
			.isBadRequest();

		client.get().uri("/api/notes/{id}", notesId).exchange().expectBody().jsonPath("$.grade").doesNotExist();
	}

	@Test
	void onlyTeachersOfTheCourseMayReviewInTheirOwnName() {
		hireProfessor("E-2");
		Long notesId = writeNotes("S-1", lectureId, "Notes").id();

		putJson(as("E-2"), "/api/notes/{id}/review", new ReviewNotesRequest("E-2", 90, null), notesId).expectStatus()
			.isForbidden();
		putJson(as("E-1"), "/api/notes/{id}/review", new ReviewNotesRequest("E-2", 90, null), notesId).expectStatus()
			.isForbidden();
		putJson(as("S-1"), "/api/notes/{id}/review", new ReviewNotesRequest("E-1", 90, null), notesId).expectStatus()
			.isForbidden();
	}

	@Test
	void reviewOfUnknownNotesReturnsNotFound() {
		putJson(as("E-1"), "/api/notes/{id}/review", new ReviewNotesRequest("E-1", 90, null), 999999L).expectStatus()
			.isNotFound();
	}

	@Test
	void studentCannotWriteNotesForSomeoneElse() {
		postJson(as("S-2"), "/api/notes", new WriteNotesRequest("S-1", lectureId, "Forged", false)).expectStatus()
			.isForbidden();
	}

	@Test
	void privateNotesAreVisibleOnlyToAuthorAndTeachers() {
		Long privateId = post(as("E-1"), "/api/notes", new WriteNotesRequest("S-1", lectureId, "Secret", true),
				NotesResponse.class)
			.id();
		writeNotes("S-2", lectureId, "Shared");

		as("S-1").get().uri("/api/notes/{id}", privateId).exchange().expectStatus().isOk();
		as("E-1").get().uri("/api/notes/{id}", privateId).exchange().expectStatus().isOk();
		as("S-2").get().uri("/api/notes/{id}", privateId).exchange().expectStatus().isForbidden();
		client.get().uri("/api/notes/{id}", privateId).exchange().expectStatus().isOk();

		as("S-2").get().uri("/api/notes?lectureId={id}", lectureId).exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].content").isEqualTo("Shared");
		as("E-1").get().uri("/api/notes?lectureId={id}", lectureId).exchange().expectBody()
			.jsonPath("$.length()").isEqualTo(2);
	}

	@Test
	void deleteRemovesNotes() {
		Long notesId = writeNotes("S-1", lectureId, "Notes").id();

		as("S-2").delete().uri("/api/notes/{id}", notesId).exchange().expectStatus().isForbidden();
		as("S-1").delete().uri("/api/notes/{id}", notesId).exchange().expectStatus().isForbidden();
		as("E-1").delete().uri("/api/notes/{id}", notesId).exchange().expectStatus().isNoContent();

		client.get().uri("/api/notes/{id}", notesId).exchange().expectStatus().isNotFound();
	}

}
