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

@Getter
@Entity
@Table(name = "students")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

	@Column(name = "student_id", nullable = false, unique = true, length = 20)
	private String studentId;

	@Setter
	@Column(nullable = false)
	private String name;

	@Setter
	@Column(name = "enrollment_year", nullable = false)
	private int enrollmentYear;

	@ManyToMany(mappedBy = "attendees")
	private Set<Lecture> attendedLectures = new HashSet<>();

	public Student(String studentId, String name, int enrollmentYear) {
		this.studentId = Objects.requireNonNull(studentId, "studentId");
		this.name = Objects.requireNonNull(name, "name");
		this.enrollmentYear = enrollmentYear;
	}

	public Set<Lecture> getAttendedLectures() {
		return Collections.unmodifiableSet(attendedLectures);
	}

	void addAttendedLecture(Lecture lecture) {
		attendedLectures.add(lecture);
	}

	void removeAttendedLecture(Lecture lecture) {
		attendedLectures.remove(lecture);
	}

}
