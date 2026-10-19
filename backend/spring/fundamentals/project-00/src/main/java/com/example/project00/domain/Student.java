package com.example.project00.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An enrolled student, identified by a unique {@code studentId}.
 * <p>
 * A student attends many lectures (*..*) and writes {@link Notes} for them. The attended-lecture set is the
 * inverse side and is maintained by {@link Lecture}. A student is enrolled in many courses; that set is maintained
 * by {@link Course}.
 */
@Getter
@Entity
@Table(name = "students")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

	@Column(name = "student_id", nullable = false, unique = true, length = 254)
	private String studentId;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Column(name = "enrollment_year", nullable = false)
	private int enrollmentYear;

	@ManyToMany(mappedBy = "attendees")
	private Set<Lecture> attendedLectures = new HashSet<>();

	@ManyToMany(mappedBy = "enrolledStudents")
	private Set<Course> enrolledCourses = new HashSet<>();

	/**
	 * Creates a student.
	 * @param studentId unique business key, e.g. {@code S-1}
	 * @param name full name
	 * @param enrollmentYear year of enrollment
	 * @throws NullPointerException if {@code studentId} or {@code name} is {@code null}
	 */
	public Student(String studentId, String name, int enrollmentYear) {
		this.studentId = Objects.requireNonNull(studentId, "studentId");
		this.name = Objects.requireNonNull(name, "name");
		this.enrollmentYear = enrollmentYear;
	}

	/** @return a read-only view of the lectures this student attends */
	public Set<Lecture> getAttendedLectures() {
		return Collections.unmodifiableSet(attendedLectures);
	}

	void addAttendedLecture(Lecture lecture) {
		attendedLectures.add(lecture);
	}

	void removeAttendedLecture(Lecture lecture) {
		attendedLectures.remove(lecture);
	}

	/** @return a read-only view of the courses this student is enrolled in */
	public Set<Course> getEnrolledCourses() {
		return Collections.unmodifiableSet(enrolledCourses);
	}

	void addEnrolledCourse(Course course) {
		enrolledCourses.add(course);
	}

	void removeEnrolledCourse(Course course) {
		enrolledCourses.remove(course);
	}

}
