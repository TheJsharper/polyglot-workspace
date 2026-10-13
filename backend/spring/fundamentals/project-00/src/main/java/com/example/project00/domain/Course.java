package com.example.project00.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
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
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

	@Column(name = "course_code", nullable = false, unique = true, length = 20)
	private String courseCode;

	@Setter
	@Column(nullable = false)
	private String title;

	@Setter
	@Column(nullable = false)
	private int credits;

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Lecture> lectures = new HashSet<>();

	public Course(String courseCode, String title, int credits) {
		this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
		this.title = Objects.requireNonNull(title, "title");
		this.credits = credits;
	}

	public Set<Lecture> getLectures() {
		return Collections.unmodifiableSet(lectures);
	}

	void addLecture(Lecture lecture) {
		lectures.add(lecture);
	}

	public void removeLecture(Lecture lecture) {
		if (lectures.remove(lecture)) {
			lecture.detach();
		}
	}

}
