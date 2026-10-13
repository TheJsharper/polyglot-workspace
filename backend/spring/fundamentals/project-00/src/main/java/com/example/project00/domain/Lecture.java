package com.example.project00.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "lectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "professor_id", nullable = false)
	private Professor professor;

	@Setter
	@Column(name = "lecture_date", nullable = false)
	private LocalDate date;

	@Setter
	@Column(name = "start_time", nullable = false)
	private LocalTime time;

	@Setter
	@Column(nullable = false)
	private String topic;

	@Setter
	@Column(nullable = false, length = 50)
	private String room;

	@ManyToMany
	@JoinTable(
			name = "lecture_attendance",
			joinColumns = @JoinColumn(name = "lecture_id"),
			inverseJoinColumns = @JoinColumn(name = "student_id"))
	private Set<Student> attendees = new HashSet<>();

	public Lecture(Course course, Professor professor, LocalDate date, LocalTime time, String topic, String room) {
		this.course = Objects.requireNonNull(course, "course");
		this.professor = Objects.requireNonNull(professor, "professor");
		this.date = Objects.requireNonNull(date, "date");
		this.time = Objects.requireNonNull(time, "time");
		this.topic = Objects.requireNonNull(topic, "topic");
		this.room = Objects.requireNonNull(room, "room");
		course.addLecture(this);
		professor.addLecture(this);
	}

	public Set<Student> getAttendees() {
		return Collections.unmodifiableSet(attendees);
	}

	public void assignProfessor(Professor newProfessor) {
		Objects.requireNonNull(newProfessor, "professor");
		professor.removeLecture(this);
		professor = newProfessor;
		newProfessor.addLecture(this);
	}

	public void addAttendee(Student student) {
		if (attendees.add(Objects.requireNonNull(student, "student"))) {
			student.addAttendedLecture(this);
		}
	}

	public void removeAttendee(Student student) {
		if (attendees.remove(student)) {
			student.removeAttendedLecture(this);
		}
	}

	public boolean hasAttendee(Student student) {
		return attendees.contains(student);
	}

	void detach() {
		professor.removeLecture(this);
		attendees.forEach(student -> student.removeAttendedLecture(this));
		attendees.clear();
	}

}
