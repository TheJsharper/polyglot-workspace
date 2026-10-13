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

/**
 * A scheduled lecture of a {@link Course}.
 * <ul>
 * <li>belongs to exactly one course (*..1)</li>
 * <li>is delivered by exactly one {@link Professor} (*..1)</li>
 * <li>is attended by many {@link Student students} (*..*), this side owns the {@code lecture_attendance} join
 * table</li>
 * </ul>
 * Both sides of every association are kept in sync by this class.
 */
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

	/**
	 * Schedules a lecture and registers it with its course and professor.
	 * @param course the course the lecture belongs to
	 * @param professor the professor delivering the lecture
	 * @param date the lecture date
	 * @param time the start time
	 * @param topic the topic
	 * @param room the room
	 * @throws NullPointerException if any argument is {@code null}
	 * @throws IllegalArgumentException if the professor does not teach the course
	 */
	public Lecture(Course course, Professor professor, LocalDate date, LocalTime time, String topic, String room) {
		this.course = Objects.requireNonNull(course, "course");
		this.professor = Objects.requireNonNull(professor, "professor");
		this.date = Objects.requireNonNull(date, "date");
		this.time = Objects.requireNonNull(time, "time");
		this.topic = Objects.requireNonNull(topic, "topic");
		this.room = Objects.requireNonNull(room, "room");
		requireTeacher(course, professor);
		course.addLecture(this);
		professor.addLecture(this);
	}

	/** @return a read-only view of the students attending this lecture */
	public Set<Student> getAttendees() {
		return Collections.unmodifiableSet(attendees);
	}

	/**
	 * Hands the lecture over to another professor, updating both professors' lecture sets.
	 * @param newProfessor the professor who delivers the lecture from now on
	 * @throws NullPointerException if {@code newProfessor} is {@code null}
	 * @throws IllegalArgumentException if the new professor does not teach the course
	 */
	public void assignProfessor(Professor newProfessor) {
		Objects.requireNonNull(newProfessor, "professor");
		requireTeacher(course, newProfessor);
		professor.removeLecture(this);
		professor = newProfessor;
		newProfessor.addLecture(this);
	}

	/**
	 * Records that a student attends this lecture; recording the same student twice has no effect.
	 * @param student the attending student
	 * @throws NullPointerException if {@code student} is {@code null}
	 * @throws IllegalArgumentException if the student is not enrolled in the course
	 */
	public void addAttendee(Student student) {
		Objects.requireNonNull(student, "student");
		if (!course.isEnrolled(student)) {
			throw new IllegalArgumentException(
					"Student " + student.getStudentId() + " is not enrolled in course " + course.getCourseCode());
		}
		if (attendees.add(student)) {
			student.addAttendedLecture(this);
		}
	}

	/**
	 * Removes a student's attendance; ignored if the student did not attend.
	 * @param student the student to remove
	 */
	public void removeAttendee(Student student) {
		if (attendees.remove(student)) {
			student.removeAttendedLecture(this);
		}
	}

	/** @return whether the given student attends this lecture */
	public boolean hasAttendee(Student student) {
		return attendees.contains(student);
	}

	private static void requireTeacher(Course course, Professor professor) {
		if (!course.isTaughtBy(professor)) {
			throw new IllegalArgumentException(
					"Professor " + professor.getEmployeeId() + " does not teach course " + course.getCourseCode());
		}
	}

	void detach() {
		professor.removeLecture(this);
		attendees.forEach(student -> student.removeAttendedLecture(this));
		attendees.clear();
	}

}
