package com.example.project00.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	public Professor(String employeeId, String name, String department) {
		this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
		this.name = Objects.requireNonNull(name, "name");
		this.department = Objects.requireNonNull(department, "department");
	}

	public Set<Lecture> getLectures() {
		return Collections.unmodifiableSet(lectures);
	}

	void addLecture(Lecture lecture) {
		lectures.add(lecture);
	}

	void removeLecture(Lecture lecture) {
		lectures.remove(lecture);
	}

}
