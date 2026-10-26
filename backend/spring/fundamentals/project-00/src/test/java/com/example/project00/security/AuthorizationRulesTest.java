package com.example.project00.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.LectureRepository;
import com.example.project00.repository.NotesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthorizationRulesTest {

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private LectureRepository lectureRepository;

	@Mock
	private NotesRepository notesRepository;

	@InjectMocks
	private AuthorizationRules authz;

	private Course course;

	private Lecture lecture;

	private Student author;

	@BeforeEach
	void setUp() {
		Professor teacher = new Professor("E-1", "Ada", "CS");
		author = new Student("S-1", "Alan", 2025);
		course = new Course("CS101", "Programming", 6);
		course.assignTeacher(teacher);
		course.enroll(author);
		lecture = new Lecture(course, teacher, LocalDate.of(2026, 10, 14), LocalTime.of(9, 0), "Variables", "A-101");
		lecture.addAttendee(author);
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void identityRulesCompareRoleAndUsername() {
		login("S-1", "STUDENT");
		assertThat(authz.isStudent("S-1")).isTrue();
		assertThat(authz.isStudent("S-2")).isFalse();
		assertThat(authz.isProfessor("S-1")).isFalse();
		assertThat(authz.isStaffOrStudent("S-1")).isTrue();
		assertThat(authz.isStaffOrStudent("S-2")).isFalse();

		login("E-1", "PROFESSOR");
		assertThat(authz.isProfessor("E-1")).isTrue();
		assertThat(authz.isStudent("E-1")).isFalse();
		assertThat(authz.isStaffOrStudent("S-2")).isTrue();
	}

	@Test
	void anonymousCallerPassesNoIdentityRule() {
		assertThat(authz.isStudent("S-1")).isFalse();
		assertThat(authz.isProfessor("E-1")).isFalse();
	}

	@Test
	void teachingRulesRequireTheCallerToTeachTheCourse() {
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));

		login("E-1", "PROFESSOR");
		assertThat(authz.teachesCourse("CS101")).isTrue();
		assertThat(authz.teachesLecture(1L)).isTrue();

		login("E-2", "PROFESSOR");
		assertThat(authz.teachesCourse("CS101")).isFalse();
		assertThat(authz.teachesLecture(1L)).isFalse();
	}

	@Test
	void missingResourcesPassSoTheServiceCanAnswer404() {
		given(courseRepository.findByCourseCode("NOPE")).willReturn(Optional.empty());
		given(lectureRepository.findById(99L)).willReturn(Optional.empty());

		login("E-1", "PROFESSOR");

		assertThat(authz.teachesCourse("NOPE")).isTrue();
		assertThat(authz.teachesLecture(99L)).isTrue();
	}

	@Test
	void privateNotesAreVisibleOnlyToAuthorAndTeachers() {
		Notes notes = new Notes(author, lecture, "Secret", true);

		login("S-1", "STUDENT");
		assertThat(authz.canRead(notes)).isTrue();
		login("S-2", "STUDENT");
		assertThat(authz.canRead(notes)).isFalse();
		login("E-1", "PROFESSOR");
		assertThat(authz.canRead(notes)).isTrue();
		login("E-2", "PROFESSOR");
		assertThat(authz.canRead(notes)).isFalse();
		login("admin", "ADMIN");
		assertThat(authz.canRead(notes)).isTrue();
	}

	@Test
	void publicNotesAreVisibleToEveryone() {
		Notes notes = new Notes(author, lecture, "Shared", false);

		login("S-2", "STUDENT");

		assertThat(authz.canRead(notes)).isTrue();
	}

	@Test
	void notesManagementAndReviewRules() {
		Notes notes = new Notes(author, lecture, "Shared", false);
		given(notesRepository.findById(5L)).willReturn(Optional.of(notes));

		login("S-1", "STUDENT");
		assertThat(authz.managesNotes(5L)).isFalse();
		assertThat(authz.reviewsNotes(5L)).isFalse();

		login("E-2", "PROFESSOR");
		assertThat(authz.managesNotes(5L)).isFalse();

		login("E-1", "PROFESSOR");
		assertThat(authz.managesNotes(5L)).isTrue();
		assertThat(authz.reviewsNotes(5L)).isTrue();
		assertThat(authz.callerTeaches(notes)).isTrue();

		login("admin", "ADMIN");
		assertThat(authz.managesNotes(5L)).isTrue();
	}

	@Test
	void adminsAndTeachersManageCoursesAndLectures() {
		given(courseRepository.findByCourseCode("CS101")).willReturn(Optional.of(course));
		given(lectureRepository.findById(1L)).willReturn(Optional.of(lecture));

		login("admin", "ADMIN");
		assertThat(authz.managesCourse("CS101")).isTrue();
		assertThat(authz.managesLecture(1L)).isTrue();

		login("E-1", "PROFESSOR");
		assertThat(authz.managesCourse("CS101")).isTrue();
		assertThat(authz.managesLecture(1L)).isTrue();

		login("E-2", "PROFESSOR");
		assertThat(authz.managesCourse("CS101")).isFalse();
		assertThat(authz.managesLecture(1L)).isFalse();

		login("S-1", "STUDENT");
		assertThat(authz.managesCourse("CS101")).isFalse();
		assertThat(authz.managesLecture(1L)).isFalse();
	}

	private static void login(String username, String role) {
		SecurityContextHolder.getContext()
			.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(username, null,
					AuthorityUtils.createAuthorityList("ROLE_" + role)));
	}

}
