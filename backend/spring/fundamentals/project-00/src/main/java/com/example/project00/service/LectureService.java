package com.example.project00.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.repository.LectureRepository;
import com.example.project00.repository.NotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for scheduling lectures and recording attendance. All methods run in a transaction.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;

	private final NotesRepository notesRepository;

	private final CourseService courseService;

	private final ProfessorService professorService;

	private final StudentService studentService;

	/**
	 * Schedules a lecture of a course, delivered by a professor.
	 * @return the persisted lecture
	 * @throws ResourceNotFoundException if the course or professor does not exist
	 */
	public Lecture schedule(String courseCode, String employeeId, LocalDate date, LocalTime time, String topic,
			String room) {
		Course course = courseService.getByCode(courseCode);
		Professor professor = professorService.getByEmployeeId(employeeId);
		return lectureRepository.save(new Lecture(course, professor, date, time, topic, room));
	}

	/**
	 * @param lectureId the lecture id
	 * @return the lecture
	 * @throws ResourceNotFoundException if no lecture has that id
	 */
	@Transactional(readOnly = true)
	public Lecture getById(Long lectureId) {
		return lectureRepository.findById(lectureId)
			.orElseThrow(() -> new ResourceNotFoundException("Lecture", lectureId));
	}

	/**
	 * @param courseCode the course code
	 * @return the course's lectures in chronological order
	 * @throws ResourceNotFoundException if no course has that code
	 */
	@Transactional(readOnly = true)
	public List<Lecture> findByCourse(String courseCode) {
		return lectureRepository.findByCourseOrderByDateAscTimeAsc(courseService.getByCode(courseCode));
	}

	/**
	 * @param employeeId the professor's employee id
	 * @return the lectures the professor delivers
	 * @throws ResourceNotFoundException if no professor has that employee id
	 */
	@Transactional(readOnly = true)
	public List<Lecture> findByProfessor(String employeeId) {
		return lectureRepository.findByProfessor(professorService.getByEmployeeId(employeeId));
	}

	/**
	 * Moves a lecture to a new date, time and room.
	 * @return the updated lecture
	 * @throws ResourceNotFoundException if no lecture has that id
	 */
	public Lecture reschedule(Long lectureId, LocalDate date, LocalTime time, String room) {
		Lecture lecture = getById(lectureId);
		lecture.setDate(date);
		lecture.setTime(time);
		lecture.setRoom(room);
		return lecture;
	}

	/**
	 * Hands a lecture over to another professor.
	 * @return the updated lecture
	 * @throws ResourceNotFoundException if the lecture or professor does not exist
	 */
	public Lecture assignProfessor(Long lectureId, String employeeId) {
		Lecture lecture = getById(lectureId);
		lecture.assignProfessor(professorService.getByEmployeeId(employeeId));
		return lecture;
	}

	/**
	 * Records that a student attends a lecture (idempotent).
	 * @return the updated lecture
	 * @throws ResourceNotFoundException if the lecture or student does not exist
	 */
	public Lecture recordAttendance(Long lectureId, String studentId) {
		Lecture lecture = getById(lectureId);
		lecture.addAttendee(studentService.getByStudentId(studentId));
		return lecture;
	}

	/**
	 * Removes a student's attendance from a lecture.
	 * @return the updated lecture
	 * @throws ResourceNotFoundException if the lecture or student does not exist
	 * @throws IllegalStateException if the student wrote notes for the lecture
	 */
	public Lecture removeAttendance(Long lectureId, String studentId) {
		Lecture lecture = getById(lectureId);
		Student student = studentService.getByStudentId(studentId);
		if (notesRepository.findByLecture(lecture).stream().anyMatch(notes -> notes.getStudent().equals(student))) {
			throw new IllegalStateException(
					"Student " + studentId + " has notes for lecture " + lectureId + " and cannot be removed");
		}
		lecture.removeAttendee(student);
		return lecture;
	}

	/**
	 * Cancels (deletes) a lecture.
	 * @throws ResourceNotFoundException if no lecture has that id
	 * @throws IllegalStateException if notes were written for the lecture
	 */
	public void cancel(Long lectureId) {
		Lecture lecture = getById(lectureId);
		if (!notesRepository.findByLecture(lecture).isEmpty()) {
			throw new IllegalStateException("Lecture " + lectureId + " has notes and cannot be cancelled");
		}
		lecture.getCourse().removeLecture(lecture);
		lectureRepository.delete(lecture);
	}

}
