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

@Service
@Transactional
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;

	private final NotesRepository notesRepository;

	private final CourseService courseService;

	private final ProfessorService professorService;

	private final StudentService studentService;

	public Lecture schedule(String courseCode, String employeeId, LocalDate date, LocalTime time, String topic,
			String room) {
		Course course = courseService.getByCode(courseCode);
		Professor professor = professorService.getByEmployeeId(employeeId);
		return lectureRepository.save(new Lecture(course, professor, date, time, topic, room));
	}

	@Transactional(readOnly = true)
	public Lecture getById(Long lectureId) {
		return lectureRepository.findById(lectureId)
			.orElseThrow(() -> new ResourceNotFoundException("Lecture", lectureId));
	}

	@Transactional(readOnly = true)
	public List<Lecture> findByCourse(String courseCode) {
		return lectureRepository.findByCourseOrderByDateAscTimeAsc(courseService.getByCode(courseCode));
	}

	@Transactional(readOnly = true)
	public List<Lecture> findByProfessor(String employeeId) {
		return lectureRepository.findByProfessor(professorService.getByEmployeeId(employeeId));
	}

	public Lecture reschedule(Long lectureId, LocalDate date, LocalTime time, String room) {
		Lecture lecture = getById(lectureId);
		lecture.setDate(date);
		lecture.setTime(time);
		lecture.setRoom(room);
		return lecture;
	}

	public Lecture assignProfessor(Long lectureId, String employeeId) {
		Lecture lecture = getById(lectureId);
		lecture.assignProfessor(professorService.getByEmployeeId(employeeId));
		return lecture;
	}

	public Lecture recordAttendance(Long lectureId, String studentId) {
		Lecture lecture = getById(lectureId);
		lecture.addAttendee(studentService.getByStudentId(studentId));
		return lecture;
	}

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

	public void cancel(Long lectureId) {
		Lecture lecture = getById(lectureId);
		if (!notesRepository.findByLecture(lecture).isEmpty()) {
			throw new IllegalStateException("Lecture " + lectureId + " has notes and cannot be cancelled");
		}
		lecture.getCourse().removeLecture(lecture);
		lectureRepository.delete(lecture);
	}

}
