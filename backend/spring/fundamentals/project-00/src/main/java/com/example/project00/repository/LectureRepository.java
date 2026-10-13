package com.example.project00.repository;

import java.util.List;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Lecture} entities.
 */
public interface LectureRepository extends JpaRepository<Lecture, Long> {

	/**
	 * @param course the course
	 * @return the course's lectures in chronological order
	 */
	List<Lecture> findByCourseOrderByDateAscTimeAsc(Course course);

	/**
	 * @param professor the professor
	 * @return the lectures the professor delivers
	 */
	List<Lecture> findByProfessor(Professor professor);

}
