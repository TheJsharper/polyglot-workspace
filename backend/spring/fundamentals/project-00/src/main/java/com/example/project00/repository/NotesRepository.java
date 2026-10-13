package com.example.project00.repository;

import java.util.List;

import com.example.project00.domain.Lecture;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Notes} entities.
 */
public interface NotesRepository extends JpaRepository<Notes, Long> {

	/**
	 * @param lecture the lecture
	 * @return all notes written for the lecture
	 */
	List<Notes> findByLecture(Lecture lecture);

	/**
	 * @param student the author
	 * @return all notes written by the student
	 */
	List<Notes> findByStudent(Student student);

	/**
	 * @return notes no professor has reviewed yet
	 */
	List<Notes> findByReviewerIsNull();

}
