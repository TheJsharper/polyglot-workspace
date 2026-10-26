package com.example.project00.security;

import com.example.project00.domain.Course;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Role;
import com.example.project00.repository.CourseRepository;
import com.example.project00.repository.LectureRepository;
import com.example.project00.repository.NotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ownership rules used in {@code @PreAuthorize} expressions as {@code @authz}.
 * <p>
 * Admins manage everything; professors manage the courses they teach; students act only on themselves.
 * <p>
 * The caller's identity is the token subject: a student id for students and an employee id for professors.
 * When the referenced resource does not exist the rule passes, so the service layer can answer with 404
 * instead of a misleading 403.
 */
@Component("authz")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationRules {

	private final CourseRepository courseRepository;

	private final LectureRepository lectureRepository;

	private final NotesRepository notesRepository;

	/** @return whether the caller is the student with the given id */
	public boolean isStudent(String studentId) {
		return hasRole(Role.STUDENT) && callerName().equals(studentId);
	}

	/** @return whether the caller is the professor with the given employee id */
	public boolean isProfessor(String employeeId) {
		return hasRole(Role.PROFESSOR) && callerName().equals(employeeId);
	}

	/** @return whether the caller is an admin, a professor, or the student with the given id */
	public boolean isStaffOrStudent(String studentId) {
		return hasRole(Role.ADMIN) || hasRole(Role.PROFESSOR) || isStudent(studentId);
	}

	/** @return whether the caller is a professor teaching the course (or the course does not exist) */
	public boolean teachesCourse(String courseCode) {
		return hasRole(Role.PROFESSOR)
				&& courseRepository.findByCourseCode(courseCode).map(this::callerTeaches).orElse(true);
	}

	/** @return whether the caller is a professor teaching the lecture's course (or the lecture does not exist) */
	public boolean teachesLecture(Long lectureId) {
		return hasRole(Role.PROFESSOR) && lectureRepository.findById(lectureId)
			.map(lecture -> callerTeaches(lecture.getCourse()))
			.orElse(true);
	}

	/** @return whether the caller is an admin or a professor teaching the course (or the course does not exist) */
	public boolean managesCourse(String courseCode) {
		return hasRole(Role.ADMIN) || teachesCourse(courseCode);
	}

	/** @return whether the caller is an admin or a professor teaching the lecture's course */
	public boolean managesLecture(Long lectureId) {
		return hasRole(Role.ADMIN) || teachesLecture(lectureId);
	}

	/** @return whether the caller may edit or delete the notes: an admin or a professor teaching their course */
	public boolean managesNotes(Long notesId) {
		return hasRole(Role.ADMIN) || reviewsNotes(notesId);
	}

	/** @return whether the caller may review the notes: a professor teaching the course they belong to */
	public boolean reviewsNotes(Long notesId) {
		return hasRole(Role.PROFESSOR) && notesRepository.findById(notesId).map(this::callerTeaches).orElse(true);
	}

	/** @return whether the caller may read the notes with the given id (or the notes do not exist) */
	public boolean canReadNotes(Long notesId) {
		return notesRepository.findById(notesId).map(this::canRead).orElse(true);
	}

	/**
	 * Public notes are visible to every authenticated user; private notes only to their author and to the
	 * professors teaching the lecture's course.
	 * @param notes the notes
	 * @return whether the caller may read them
	 */
	public boolean canRead(Notes notes) {
		if (!notes.isPrivate() || hasRole(Role.ADMIN)) {
			return true;
		}
		return (hasRole(Role.STUDENT) && notes.getStudent().getStudentId().equals(callerName()))
				|| (hasRole(Role.PROFESSOR) && callerTeaches(notes));
	}

	/** @return whether the caller is a professor teaching the course the notes belong to */
	public boolean callerTeaches(Notes notes) {
		return hasRole(Role.PROFESSOR) && callerTeaches(notes.getLecture().getCourse());
	}

	private boolean callerTeaches(Course course) {
		String caller = callerName();
		return course.getTeachers().stream().anyMatch(professor -> professor.getEmployeeId().equals(caller));
	}

	private static boolean hasRole(Role role) {
		Authentication authentication = currentAuthentication();
		return authentication != null && authentication.getAuthorities()
			.stream()
			.anyMatch(authority -> role.authority().equals(authority.getAuthority()));
	}

	private static String callerName() {
		Authentication authentication = currentAuthentication();
		return authentication == null ? "" : authentication.getName();
	}

	private static Authentication currentAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
				|| !authentication.isAuthenticated()) {
			return null;
		}
		return authentication;
	}

}
