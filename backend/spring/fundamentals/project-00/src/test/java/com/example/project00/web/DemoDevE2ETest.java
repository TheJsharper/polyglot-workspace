package com.example.project00.web;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.annotation.Transactional;

import com.example.project00.domain.Course;
import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.ProfessorRepository;
import com.example.project00.repository.StudentRepository;
import com.example.project00.service.DemoKeyService;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-dev-test;DB_CLOSE_DELAY=-1")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class DemoDevE2ETest {

	@Autowired
	private RestTestClient client;

	@Autowired
	private DemoKeyService demoKeys;

	@Autowired
	private CourseRepository courses;

	@Autowired
	private StudentRepository students;

	@Autowired
	private ProfessorRepository professors;

	@Test
	@Transactional
	void devSeedHasTenOfEachAndCompleteRelationships() {
		assertThat(students.count()).isEqualTo(10);
		assertThat(professors.count()).isEqualTo(10);
		assertThat(courses.count()).isEqualTo(10);
		List<Course> allCourses = courses.findAll();
		assertThat(allCourses).allSatisfy(course -> {
			assertThat(course.getTeachers()).hasSize(1);
			assertThat(course.getEnrolledStudents()).hasSize(2);
		});
		assertThat(professors.findAll()).allSatisfy(professor -> assertThat(professor.getTaughtCourses()).hasSize(1));
		assertThat(students.findAll()).allSatisfy(student -> assertThat(student.getEnrolledCourses()).hasSize(2));
	}

	@Test
	void localDemoCanBeRedeemedButCannotWrite() throws Exception {
		client.get().uri("/api/demo/status").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.enabled").isEqualTo(true);

		Map<?, ?> redeemed = client.post()
			.uri("/api/demo/redeem")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Map.of("token", demoKeys.create(1, 1, "e2e", "manager").get(0).getToken(), "persona", "STUDENT"))
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(Map.class)
			.returnResult()
			.getResponseBody();
		String token = (String) redeemed.get("accessToken");
		RestTestClient demo = client.mutate().defaultHeader("Authorization", "Bearer " + token).build();

		demo.get().uri("/api/auth/me").exchange().expectStatus().isOk().expectBody()
			.jsonPath("$.username").isEqualTo("stud-anna");
		demo.post()
			.uri("/api/courses")
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body(Map.of("courseCode", "WRITE", "title", "Should be blocked", "credits", 3))
			.exchange()
			.expectStatus()
			.isForbidden();
	}

}
