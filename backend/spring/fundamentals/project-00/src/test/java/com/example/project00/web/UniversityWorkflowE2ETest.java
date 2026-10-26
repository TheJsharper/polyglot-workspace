package com.example.project00.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.project00.web.dto.CourseDtos.CourseResponse;
import com.example.project00.web.dto.LectureDtos.LectureResponse;
import com.example.project00.web.dto.LectureDtos.ScheduleLectureRequest;
import com.example.project00.web.dto.NotesDtos.NotesResponse;
import com.example.project00.web.dto.NotesDtos.ReviewNotesRequest;
import org.junit.jupiter.api.Test;

/**
 * Walks through a full semester flow across every endpoint group, each step performed by the role allowed to do it.
 */
class UniversityWorkflowE2ETest extends EndToEndTestSupport {

	@Test
	void courseLifecycleFromSchedulingToGradedNotes() {
		// The admin sets up the catalog and staff.
		createCourse("CS101");
		hireProfessor("E-1");
		assignTeacher("CS101", "E-1");

		// Students register themselves and sign up for the course.
		enrollStudent("S-1");
		enrollStudent("S-2");
		enrollInCourse("CS101", "S-1");
		CourseResponse course = enrollInCourse("CS101", "S-2");
		assertThat(course.teacherIds()).containsExactly("E-1");
		assertThat(course.enrolledStudentIds()).containsExactly("S-1", "S-2");

		// The teacher schedules the lecture and takes attendance.
		LectureResponse lecture = post(as("E-1"), "/api/lectures",
				new ScheduleLectureRequest("CS101", "E-1", DATE, TIME, "Variables", "A-101"), LectureResponse.class);
		recordAttendance(lecture.id(), "S-1");
		assertThat(recordAttendance(lecture.id(), "S-2").attendeeIds()).containsExactly("S-1", "S-2");

		NotesResponse alanNotes = writeNotes("S-1", lecture.id(), "Variables hold values");
		NotesResponse graceNotes = writeNotes("S-2", lecture.id(), "Use meaningful names");

		NotesResponse graded = putJson(as("E-1"), "/api/notes/{id}/review",
				new ReviewNotesRequest("E-1", 88, "Good start"), alanNotes.id())
			.expectStatus()
			.isOk()
			.expectBody(NotesResponse.class)
			.returnResult()
			.getResponseBody();
		assertThat(graded.grade()).isEqualTo(88);

		as("E-1").get().uri("/api/notes/pending-review").exchange().expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].studentId").isEqualTo("S-2");

		// Notes protect the lecture and its participants from being removed.
		as("E-1").delete().uri("/api/lectures/{id}", lecture.id()).exchange().expectStatus().isEqualTo(409);
		client.delete().uri("/api/professors/E-1").exchange().expectStatus().isEqualTo(409);
		client.delete().uri("/api/students/S-1").exchange().expectStatus().isEqualTo(409);

		// The teacher removes the notes, then the lecture and people can go.
		as("E-1").delete().uri("/api/notes/{id}", alanNotes.id()).exchange().expectStatus().isNoContent();
		as("E-1").delete().uri("/api/notes/{id}", graceNotes.id()).exchange().expectStatus().isNoContent();

		as("E-1").delete().uri("/api/lectures/{id}", lecture.id()).exchange().expectStatus().isNoContent();
		client.delete().uri("/api/professors/E-1").exchange().expectStatus().isNoContent();
		client.delete().uri("/api/students/S-1").exchange().expectStatus().isNoContent();

		// Deleting people also removes their accounts.
		postJson(anonymous, "/api/auth/login", new com.example.project00.web.dto.AuthDtos.LoginRequest("E-1", PASSWORD))
			.expectStatus()
			.isUnauthorized();
		client.get().uri("/api/courses/CS101").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.teacherIds.length()").isEqualTo(0)
			.jsonPath("$.enrolledStudentIds.length()").isEqualTo(1);
	}

}