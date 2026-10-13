package com.example.project00.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A course in the catalogue, identified by its unique {@code courseCode}.
 * <p>
 * A course owns its lectures (1..*): lectures are created through
 * {@link Lecture#Lecture(Course, Professor, java.time.LocalDate, java.time.LocalTime, String, String)} and
 * deleted together with the course (cascade + orphan removal).
 * <p>
 * The course also owns two many-to-many associations: the students {@linkplain #enroll(Student) enrolled} in
 * it ({@code course_enrollment}) and the professors who {@linkplain #assignTeacher(Professor) teach} it
 * ({@code course_teaching}). Only teachers may deliver its lectures and only enrolled students may attend them.
 */
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

	@ManyToMany
	@JoinTable(
			name = "course_enrollment",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "student_id"))
	private Set<Student> enrolledStudents = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "course_teaching",
			joinColumns = @JoinColumn(name = "course_id"),
			inverseJoinColumns = @JoinColumn(name = "professor_id"))
	private Set<Professor> teachers = new HashSet<>();

	/**
	 * Creates a course.
	 * @param courseCode unique business key, e.g. {@code CS101}
	 * @param title human-readable title
	 * @param credits credit points awarded on completion
	 * @throws NullPointerException if {@code courseCode} or {@code title} is {@code null}
	 */
	public Course(String courseCode, String title, int credits) {
		this.courseCode = Objects.requireNonNull(courseCode, "courseCode");
		this.title = Objects.requireNonNull(title, "title");
		this.credits = credits;
	}

	/** @return a read-only view of the lectures scheduled for this course */
	public Set<Lecture> getLectures() {
		return Collections.unmodifiableSet(lectures);
	}

	void addLecture(Lecture lecture) {
		lectures.add(lecture);
	}

	/**
	 * Removes a lecture from this course and detaches it from its professor and attendees, so that it is
	 * deleted as an orphan on the next flush.
	 * @param lecture the lecture to remove; ignored if it does not belong to this course
	 */
	public void removeLecture(Lecture lecture) {
		if (lectures.remove(lecture)) {
			lecture.detach();
		}
	}

	/** @return a read-only view of the students enrolled in this course */
	public Set<Student> getEnrolledStudents() {
		return Collections.unmodifiableSet(enrolledStudents);
	}

	/** @return a read-only view of the professors teaching this course */
	public Set<Professor> getTeachers() {
		return Collections.unmodifiableSet(teachers);
	}

	/**
	 * Enrolls a student in this course; enrolling the same student twice has no effect.
	 * @param student the student to enroll
	 * @throws NullPointerException if {@code student} is {@code null}
	 */
	public void enroll(Student student) {
		if (enrolledStudents.add(Objects.requireNonNull(student, "student"))) {
			student.addEnrolledCourse(this);
		}
	}

	/**
	 * Removes a student's enrollment; ignored if the student is not enrolled.
	 * @param student the student to unenroll
	 * @throws IllegalStateException if the student still attends lectures of this course
	 */
	public void unenroll(Student student) {
		if (lectures.stream().anyMatch(lecture -> lecture.hasAttendee(student))) {
			throw new IllegalStateException("Student " + student.getStudentId() + " still attends lectures of course "
					+ courseCode + " and cannot be unenrolled");
		}
		if (enrolledStudents.remove(student)) {
			student.removeEnrolledCourse(this);
		}
	}

	/** @return whether the given student is enrolled in this course */
	public boolean isEnrolled(Student student) {
		return enrolledStudents.contains(student);
	}

	/**
	 * Lets a professor teach this course; assigning the same professor twice has no effect.
	 * @param professor the professor
	 * @throws NullPointerException if {@code professor} is {@code null}
	 */
	public void assignTeacher(Professor professor) {
		if (teachers.add(Objects.requireNonNull(professor, "professor"))) {
			professor.addTaughtCourse(this);
		}
	}

	/**
	 * Stops a professor from teaching this course; ignored if they do not teach it.
	 * @param professor the professor
	 * @throws IllegalStateException if the professor still delivers lectures of this course
	 */
	public void unassignTeacher(Professor professor) {
		if (lectures.stream().anyMatch(lecture -> lecture.getProfessor().equals(professor))) {
			throw new IllegalStateException("Professor " + professor.getEmployeeId()
					+ " still delivers lectures of course " + courseCode + " and cannot be unassigned");
		}
		if (teachers.remove(professor)) {
			professor.removeTaughtCourse(this);
		}
	}

	/** @return whether the given professor teaches this course */
	public boolean isTaughtBy(Professor professor) {
		return teachers.contains(professor);
	}

}
