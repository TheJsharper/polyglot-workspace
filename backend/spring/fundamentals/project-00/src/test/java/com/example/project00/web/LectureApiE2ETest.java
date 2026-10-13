package com.example.project00.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;

import com.example.project00.web.dto.LectureDtos.AssignProfessorRequest;
import com.example.project00.web.dto.LectureDtos.LectureResponse;
import com.example.project00.web.dto.LectureDtos.RescheduleLectureRequest;
import com.example.project00.web.dto.LectureDtos.ScheduleLectureRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

class LectureApiE2ETest extends EndToEndTestSupport {

	@BeforeEach
	void createReferenceData() {
		createCourse("CS101");
		hireProfessor("E-1");
		enrollStudent("S-1");
	}

	@Test
	void scheduleReturnsCreatedLecture() {
		assignTeacher("CS101", "E-1");

		LectureResponse lecture = postJson(as("E-1"), "/api/lectures",
				new ScheduleLectureRequest("CS101", "E-1", DATE, TIME, "Variables", "A-101"))
			.expectStatus()
			.isCreated()
			.expectBody(LectureResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(lecture.id()).isNotNull();
		assertThat(lecture.courseCode()).isEqualTo("CS101");
		assertThat(lecture.employeeId()).isEqualTo("E-1");
		assertThat(lecture.date()).isEqualTo(DATE);
		assertThat(lecture.time()).isEqualTo(TIME);
		assertThat(lecture.topic()).isEqualTo("Variables");
		assertThat(lecture.room()).isEqualTo("A-101");
		assertThat(lecture.attendeeIds()).isEmpty();
	}

	@Test
	void scheduleForUnknownCourseReturnsNotFound() {
		postJson(as("E-1"), "/api/lectures", new ScheduleLectureRequest("NOPE", "E-1", DATE, TIME, "Variables", "A-101"))
			.expectStatus()
			.isNotFound()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Course 'NOPE' not found");
	}

	@Test
	void scheduleWithMissingFieldsReturnsBadRequest() {
		assignTeacher("CS101", "E-1");

		postJson(as("E-1"), "/api/lectures", new ScheduleLectureRequest("CS101", "E-1", null, null, "", "A-101"))
			.expectStatus()
			.isBadRequest();
	}

	@Test
	void onlyTeachersOfTheCourseMaySchedule() {
		ScheduleLectureRequest request = new ScheduleLectureRequest("CS101", "E-1", DATE, TIME, "Variables", "A-101");

		postJson(as("E-1"), "/api/lectures", request).expectStatus().isForbidden();
		postJson(client, "/api/lectures", request).expectStatus().isForbidden();
		postJson(as("S-1"), "/api/lectures", request).expectStatus().isForbidden();
		postJson(anonymous, "/api/lectures", request).expectStatus().isUnauthorized();
	}

	@Test
	void teacherCannotScheduleLectureForAnotherProfessorWhoDoesNotTeach() {
		hireProfessor("E-2");
		assignTeacher("CS101", "E-1");

		postJson(as("E-1"), "/api/lectures", new ScheduleLectureRequest("CS101", "E-2", DATE, TIME, "Loops", "A-101"))
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Professor E-2 does not teach course CS101");
	}

	@Test
	void findByCourseReturnsLecturesInChronologicalOrder() {
		createCourse("CS102");
		scheduleLecture("CS101", "E-1", DATE, LocalTime.of(14, 0), "Afternoon");
		scheduleLecture("CS101", "E-1", DATE, LocalTime.of(8, 0), "Morning");
		scheduleLecture("CS101", "E-1", DATE.minusDays(1), TIME, "Yesterday");
		scheduleLecture("CS102", "E-1", DATE, TIME, "Other course");

		List<LectureResponse> lectures = client.get()
			.uri("/api/lectures?courseCode=CS101")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<LectureResponse>>() {
			})
			.returnResult()
			.getResponseBody();

		assertThat(lectures).extracting(LectureResponse::topic).containsExactly("Yesterday", "Morning", "Afternoon");
	}

	@Test
	void findByProfessorReturnsOnlyTheirLectures() {
		hireProfessor("E-2");
		scheduleLecture("CS101", "E-1", DATE, TIME, "Variables");
		scheduleLecture("CS101", "E-2", DATE, TIME, "Loops");

		client.get().uri("/api/lectures?employeeId=E-2").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].topic").isEqualTo("Loops");
	}

	@Test
	void rescheduleUpdatesDateTimeAndRoom() {
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();

		putJson(as("S-1"), "/api/lectures/{id}/schedule",
				new RescheduleLectureRequest(DATE.plusDays(7), LocalTime.of(11, 30), "B-202"), lectureId)
			.expectStatus()
			.isForbidden();

		LectureResponse lecture = putJson(as("E-1"), "/api/lectures/{id}/schedule",
				new RescheduleLectureRequest(DATE.plusDays(7), LocalTime.of(11, 30), "B-202"), lectureId)
			.expectStatus()
			.isOk()
			.expectBody(LectureResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(lecture.date()).isEqualTo(DATE.plusDays(7));
		assertThat(lecture.time()).isEqualTo(LocalTime.of(11, 30));
		assertThat(lecture.room()).isEqualTo("B-202");
	}

	@Test
	void assignProfessorMovesLecture() {
		hireProfessor("E-2");
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		assignTeacher("CS101", "E-2");

		putJson("/api/lectures/{id}/professor", new AssignProfessorRequest("E-2"), lectureId).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.employeeId").isEqualTo("E-2");

		client.get().uri("/api/lectures?employeeId=E-1").exchange().expectBody().json("[]");
	}

	@Test
	void attendanceCanBeRecordedAndRemoved() {
		enrollStudent("S-2");
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();

		recordAttendance(lectureId, "S-2");
		assertThat(recordAttendance(lectureId, "S-1").attendeeIds()).containsExactly("S-1", "S-2");

		as("E-1").delete()
			.uri("/api/lectures/{id}/attendees/{studentId}", lectureId, "S-1")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.attendeeIds.length()").isEqualTo(1)
			.jsonPath("$.attendeeIds[0]").isEqualTo("S-2");
	}

	@Test
	void recordingAttendanceRequiresEnrollmentInTheCourse() {
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();

		as("E-1").put()
			.uri("/api/lectures/{id}/attendees/{studentId}", lectureId, "S-1")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Student S-1 is not enrolled in course CS101");
	}

	@Test
	void recordingAttendanceForUnknownStudentReturnsNotFound() {
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();

		as("E-1").put()
			.uri("/api/lectures/{id}/attendees/{studentId}", lectureId, "NOPE")
			.exchange()
			.expectStatus()
			.isNotFound();
	}

	@Test
	void removingAttendanceOfStudentWithNotesReturnsConflict() {
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");
		writeNotes("S-1", lectureId, "Variables hold values");

		as("E-1").delete()
			.uri("/api/lectures/{id}/attendees/{studentId}", lectureId, "S-1")
			.exchange()
			.expectStatus()
			.isEqualTo(409);
	}

	@Test
	void cancelDeletesLectureWithoutNotes() {
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");

		client.delete().uri("/api/lectures/{id}", lectureId).exchange().expectStatus().isForbidden();
		as("E-1").delete().uri("/api/lectures/{id}", lectureId).exchange().expectStatus().isNoContent();

		client.get().uri("/api/lectures/{id}", lectureId).exchange().expectStatus().isNotFound();
		client.get().uri("/api/students/S-1").exchange().expectStatus().isOk();
	}

	@Test
	void cancelRejectsLectureWithNotes() {
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");
		writeNotes("S-1", lectureId, "Variables hold values");

		as("E-1").delete().uri("/api/lectures/{id}", lectureId).exchange().expectStatus().isEqualTo(409).expectBody()
			.jsonPath("$.detail").isEqualTo("Lecture " + lectureId + " has notes and cannot be cancelled");
	}

	@Test
	void unknownLectureReturnsNotFound() {
		client.get().uri("/api/lectures/999999").exchange().expectStatus().isNotFound();
	}

}
