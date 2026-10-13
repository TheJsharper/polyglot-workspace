package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Notes;
import com.example.project00.repository.NotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotesService {

	private final NotesRepository notesRepository;

	private final StudentService studentService;

	private final LectureService lectureService;

	private final ProfessorService professorService;

	public Notes write(String studentId, Long lectureId, String content, boolean isPrivate) {
		return notesRepository.save(new Notes(studentService.getByStudentId(studentId),
				lectureService.getById(lectureId), content, isPrivate));
	}

	@Transactional(readOnly = true)
	public Notes getById(Long notesId) {
		return notesRepository.findById(notesId).orElseThrow(() -> new ResourceNotFoundException("Notes", notesId));
	}

	@Transactional(readOnly = true)
	public List<Notes> findByLecture(Long lectureId) {
		return notesRepository.findByLecture(lectureService.getById(lectureId));
	}

	@Transactional(readOnly = true)
	public List<Notes> findByStudent(String studentId) {
		return notesRepository.findByStudent(studentService.getByStudentId(studentId));
	}

	@Transactional(readOnly = true)
	public List<Notes> findPendingReview() {
		return notesRepository.findByReviewerIsNull();
	}

	public Notes edit(Long notesId, String content, boolean isPrivate) {
		Notes notes = getById(notesId);
		notes.setContent(content);
		notes.setPrivate(isPrivate);
		return notes;
	}

	public Notes review(Long notesId, String employeeId, int grade, String comment) {
		Notes notes = getById(notesId);
		notes.review(professorService.getByEmployeeId(employeeId), grade, comment);
		return notes;
	}

	public void delete(Long notesId) {
		notesRepository.delete(getById(notesId));
	}

}
