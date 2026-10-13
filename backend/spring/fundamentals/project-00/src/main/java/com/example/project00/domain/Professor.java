package com.example.project00.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A member of the teaching staff, identified by a unique {@code employeeId}.
 * <p>
 * A professor delivers many lectures (1..*) and may review student {@link Notes}. The lecture set is the
 * inverse side and is maintained by {@link Lecture}. A professor teaches many courses; that set is maintained by
 * {@link Course}.
 */
@Getter
@Entity
@Table(name = "professors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Professor extends BaseEntity {

	@Column(name = "employee_id", nullable = false, unique = true, length = 20)
	private String employeeId;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Column(nullable = false)
	private String department;

	@OneToMany(mappedBy = "professor")
	private Set<Lecture> lectures = new HashSet<>();

	@ManyToMany(mappedBy = "teachers")
	private Set<Course> taughtCourses = new HashSet<>();

	/**
	 * Creates a professor.
	 * @param employeeId unique business key, e.g. {@code E-1}
	 * @param name full name
	 * @param department department name
	 * @throws NullPointerException if any argument is {@code null}
	 */
	public Professor(String employeeId, String name, String department) {
		this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
		this.name = Objects.requireNonNull(name, "name");
		this.department = Objects.requireNonNull(department, "department");
	}

	/** @return a read-only view of the lectures this professor delivers */
	public Set<Lecture> getLectures() {
		return Collections.unmodifiableSet(lectures);
	}

	void addLecture(Lecture lecture) {
		lectures.add(lecture);
	}

	void removeLecture(Lecture lecture) {
		lectures.remove(lecture);
	}

	/** @return a read-only view of the courses this professor teaches */
	public Set<Course> getTaughtCourses() {
		return Collections.unmodifiableSet(taughtCourses);
	}

	void addTaughtCourse(Course course) {
		taughtCourses.add(course);
	}

	void removeTaughtCourse(Course course) {
		taughtCourses.remove(course);
	}

}
