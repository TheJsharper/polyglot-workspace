package com.example.project00.web;

import com.example.project00.web.dto.StudentDtos.CreateStudentRequest;
import com.example.project00.web.dto.StudentDtos.UpdateStudentRequest;
import org.junit.jupiter.api.Test;

class StudentApiE2ETest extends EndToEndTestSupport {

	@Test
	void enrollReturnsCreatedStudent() {
		postJson("/api/students", new CreateStudentRequest("S-1", "Alan", 2025)).expectStatus()
			.isCreated()
			.expectHeader()
			.location("/api/students/S-1")
			.expectBody()
			.jsonPath("$.studentId").isEqualTo("S-1")
			.jsonPath("$.name").isEqualTo("Alan")
			.jsonPath("$.enrollmentYear").isEqualTo(2025);
	}

	@Test
	void getAndListReturnStudents() {
		enrollStudent("S-1");
		enrollStudent("S-2");

		client.get().uri("/api/students/S-1").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.name").isEqualTo("Student S-1");
		client.get().uri("/api/students").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(2);
	}

	@Test
	void updateChangesNameAndEnrollmentYear() {
		enrollStudent("S-1");

		putJson("/api/students/S-1", new UpdateStudentRequest("Alan Turing", 2024)).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.name").isEqualTo("Alan Turing")
			.jsonPath("$.enrollmentYear").isEqualTo(2024);
	}

	@Test
	void deleteRemovesStudentAndTheirAttendance() {
		createCourse("CS101");
		hireProfessor("E-1");
		enrollStudent("S-1");
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");

		client.delete().uri("/api/students/S-1").exchange().expectStatus().isNoContent();

		client.get().uri("/api/students/S-1").exchange().expectStatus().isNotFound();
		client.get().uri("/api/lectures/{id}", lectureId).exchange().expectBody()
			.jsonPath("$.attendeeIds.length()").isEqualTo(0);
	}

	@Test
	void deleteRejectsStudentWithNotes() {
		createCourse("CS101");
		hireProfessor("E-1");
		enrollStudent("S-1");
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();
		recordAttendance(lectureId, "S-1");
		writeNotes("S-1", lectureId, "Variables hold values");

		client.delete().uri("/api/students/S-1").exchange().expectStatus().isEqualTo(409).expectBody()
			.jsonPath("$.detail").isEqualTo("Student S-1 still has notes");
	}

	@Test
	void duplicateStudentIdReturnsConflict() {
		enrollStudent("S-1");

		postJson("/api/students", new CreateStudentRequest("S-1", "Grace", 2024)).expectStatus().isEqualTo(409);
	}

	@Test
	void invalidRequestReturnsBadRequest() {
		postJson("/api/students", new CreateStudentRequest("S-1", "Alan", 1800)).expectStatus().isBadRequest();
	}

}
