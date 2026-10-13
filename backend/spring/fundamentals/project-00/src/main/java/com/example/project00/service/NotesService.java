package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Notes;
import com.example.project00.repository.NotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases for writing, editing and reviewing lecture notes. All methods run in a transaction.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class NotesService {

	private final NotesRepository notesRepository;

	private final StudentService studentService;

	private final LectureService lectureService;

	private final ProfessorService professorService;

	/**
	 * Writes notes for a lecture the student attended.
	 * @return the persisted notes
	 * @throws ResourceNotFoundException if the student or lecture does not exist
	 * @throws IllegalArgumentException if the student did not attend the lecture
	 */
	public Notes write(String studentId, Long lectureId, String content, boolean isPrivate) {
		return notesRepository.save(new Notes(studentService.getByStudentId(studentId),
				lectureService.getById(lectureId), content, isPrivate));
	}

	/**
	 * @param notesId the notes id
	 * @return the notes
	 * @throws ResourceNotFoundException if no notes has that id
	 */
	@Transactional(readOnly = true)
	public Notes getById(Long notesId) {
		return notesRepository.findById(notesId).orElseThrow(() -> new ResourceNotFoundException("Notes", notesId));
	}

	/**
	 * @param lectureId the lecture id
	 * @return all notes written for the lecture
	 * @throws ResourceNotFoundException if no lecture has that id
	 */
	@Transactional(readOnly = true)
	public List<Notes> findByLecture(Long lectureId) {
		return notesRepository.findByLecture(lectureService.getById(lectureId));
	}

	/**
	 * @param studentId the author's student id
	 * @return all notes written by the student
	 * @throws ResourceNotFoundException if no student has that student id
	 */
	@Transactional(readOnly = true)
	public List<Notes> findByStudent(String studentId) {
		return notesRepository.findByStudent(studentService.getByStudentId(studentId));
	}

	/** @return notes no professor has reviewed yet */
	@Transactional(readOnly = true)
	public List<Notes> findPendingReview() {
		return notesRepository.findByReviewerIsNull();
	}

	/**
	 * Changes the content and visibility of notes.
	 * @return the updated notes
	 * @throws ResourceNotFoundException if no notes has that id
	 */
	public Notes edit(Long notesId, String content, boolean isPrivate) {
		Notes notes = getById(notesId);
		notes.setContent(content);
		notes.setPrivate(isPrivate);
		return notes;
	}

	/**
	 * Reviews and grades notes on behalf of a professor.
	 * @return the reviewed notes
	 * @throws ResourceNotFoundException if the notes or professor does not exist
	 * @throws IllegalArgumentException if the grade is out of range
	 */
	public Notes review(Long notesId, String employeeId, int grade, String comment) {
		Notes notes = getById(notesId);
		notes.review(professorService.getByEmployeeId(employeeId), grade, comment);
		return notes;
	}

	/**
	 * Deletes notes.
	 * @throws ResourceNotFoundException if no notes has that id
	 */
	public void delete(Long notesId) {
		notesRepository.delete(getById(notesId));
	}

}
