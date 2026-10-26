package com.example.project00.web;

import com.example.project00.web.dto.ProfessorDtos.CreateProfessorRequest;
import com.example.project00.web.dto.ProfessorDtos.UpdateProfessorRequest;
import org.junit.jupiter.api.Test;

class ProfessorApiE2ETest extends EndToEndTestSupport {

	@Test
	void hireReturnsCreatedProfessor() {
		postJson("/api/professors", new CreateProfessorRequest("E-1", "Ada", "Computer Science", PASSWORD)).expectStatus()
			.isCreated()
			.expectHeader()
			.location("/api/professors/E-1")
			.expectBody()
			.jsonPath("$.employeeId").isEqualTo("E-1")
			.jsonPath("$.name").isEqualTo("Ada")
			.jsonPath("$.department").isEqualTo("Computer Science")
			.jsonPath("$.password").doesNotExist();

		login("E-1", PASSWORD);
	}

	@Test
	void getAndListReturnProfessors() {
		hireProfessor("E-1");
		hireProfessor("E-2");

		client.get().uri("/api/professors/E-2").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.name").isEqualTo("Professor E-2");
		client.get().uri("/api/professors").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(2);
	}

	@Test
	void updateChangesNameAndDepartment() {
		hireProfessor("E-1");

		putJson("/api/professors/E-1", new UpdateProfessorRequest("Ada Lovelace", "Mathematics")).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.name").isEqualTo("Ada Lovelace")
			.jsonPath("$.department").isEqualTo("Mathematics");
	}

	@Test
	void deleteRemovesProfessorWithoutLectures() {
		hireProfessor("E-1");

		client.delete().uri("/api/professors/E-1").exchange().expectStatus().isNoContent();

		client.get().uri("/api/professors/E-1").exchange().expectStatus().isNotFound();
	}

	@Test
	void deleteRejectsProfessorWhoStillDeliversLectures() {
		createCourse("CS101");
		hireProfessor("E-1");
		scheduleLecture("CS101", "E-1", DATE, TIME, "Variables");

		client.delete().uri("/api/professors/E-1").exchange().expectStatus().isEqualTo(409).expectBody()
			.jsonPath("$.detail").isEqualTo("Professor E-1 still delivers 1 lecture(s)");

		client.get().uri("/api/professors/E-1").exchange().expectStatus().isOk();
	}

	@Test
	void duplicateEmployeeIdReturnsConflict() {
		hireProfessor("E-1");

		postJson("/api/professors", new CreateProfessorRequest("E-1", "Grace", "Math", PASSWORD)).expectStatus().isEqualTo(409);
	}

	@Test
	void invalidRequestReturnsBadRequest() {
		postJson("/api/professors", new CreateProfessorRequest("", null, "", "short")).expectStatus().isBadRequest();
	}

	@Test
	void unknownProfessorReturnsNotFound() {
		client.get().uri("/api/professors/NOPE").exchange().expectStatus().isNotFound().expectBody()
			.jsonPath("$.detail").isEqualTo("Professor 'NOPE' not found");
	}

	@Test
	void professorsSeeStudentsEnrolledInTheirCourses() {
		createCourse("CS101");
		createCourse("CS102");
		hireProfessor("E-1");
		hireProfessor("E-2");
		assignTeacher("CS101", "E-1");
		assignTeacher("CS102", "E-2");
		enrollStudent("S-1");
		enrollStudent("S-2");
		enrollInCourse("CS101", "S-1");
		enrollInCourse("CS102", "S-2");

		as("E-1").get().uri("/api/professors/E-1/students").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].studentId").isEqualTo("S-1");
		client.get().uri("/api/professors/E-2/students").exchange().expectStatus().isOk();
		as("E-1").get().uri("/api/professors/E-2/students").exchange().expectStatus().isForbidden();
		as("S-1").get().uri("/api/professors/E-1/students").exchange().expectStatus().isForbidden();
	}

}
