package com.example.project00.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.example.project00.web.dto.CourseDtos.CourseResponse;
import com.example.project00.web.dto.CourseDtos.CreateCourseRequest;
import com.example.project00.web.dto.CourseDtos.UpdateCourseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

class CourseApiE2ETest extends EndToEndTestSupport {

	@Test
	void createReturnsCreatedCourseWithLocation() {
		postJson("/api/courses", new CreateCourseRequest("CS101", "Programming", 6)).expectStatus()
			.isCreated()
			.expectHeader()
			.location("/api/courses/CS101")
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.courseCode").isEqualTo("CS101")
			.jsonPath("$.title").isEqualTo("Programming")
			.jsonPath("$.credits").isEqualTo(6);
	}

	@Test
	void getReturnsExistingCourse() {
		createCourse("CS101");

		CourseResponse course = client.get()
			.uri("/api/courses/CS101")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(CourseResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(course.courseCode()).isEqualTo("CS101");
		assertThat(course.title()).isEqualTo("Course CS101");
	}

	@Test
	void listReturnsAllCourses() {
		createCourse("CS101");
		createCourse("CS102");

		List<CourseResponse> courses = client.get()
			.uri("/api/courses")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(new ParameterizedTypeReference<List<CourseResponse>>() {
			})
			.returnResult()
			.getResponseBody();

		assertThat(courses).extracting(CourseResponse::courseCode).containsExactlyInAnyOrder("CS101", "CS102");
	}

	@Test
	void updateChangesTitleAndCredits() {
		createCourse("CS101");

		putJson("/api/courses/CS101", new UpdateCourseRequest("Programming I", 8)).expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("$.title").isEqualTo("Programming I")
			.jsonPath("$.credits").isEqualTo(8);

		client.get().uri("/api/courses/CS101").exchange().expectBody().jsonPath("$.title").isEqualTo("Programming I");
	}

	@Test
	void deleteRemovesCourse() {
		createCourse("CS101");

		client.delete().uri("/api/courses/CS101").exchange().expectStatus().isNoContent();

		client.get().uri("/api/courses/CS101").exchange().expectStatus().isNotFound();
	}

	@Test
	void deleteCascadesToLecturesWithoutNotes() {
		createCourse("CS101");
		hireProfessor("E-1");
		Long lectureId = scheduleLecture("CS101", "E-1", DATE, TIME, "Variables").id();

		client.delete().uri("/api/courses/CS101").exchange().expectStatus().isNoContent();

		client.get().uri("/api/lectures/{id}", lectureId).exchange().expectStatus().isNotFound();
	}

	@Test
	void unknownCourseReturnsProblemDetail() {
		client.get()
			.uri("/api/courses/NOPE")
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectHeader()
			.contentType("application/problem+json")
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.title").isEqualTo("Resource not found")
			.jsonPath("$.detail").isEqualTo("Course 'NOPE' not found");
	}

	@Test
	void duplicateCourseCodeReturnsConflict() {
		createCourse("CS101");

		postJson("/api/courses", new CreateCourseRequest("CS101", "Other", 3)).expectStatus()
			.isEqualTo(409)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Course 'CS101' already exists");
	}

	@Test
	void invalidRequestReturnsBadRequest() {
		postJson("/api/courses", new CreateCourseRequest(" ", "", 0)).expectStatus().isBadRequest();

		client.get().uri("/api/courses").exchange().expectBody().json("[]");
	}

}
