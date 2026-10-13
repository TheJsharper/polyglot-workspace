package com.example.project00.repository;

import java.util.List;

import com.example.project00.domain.Course;
import com.example.project00.domain.Lecture;
import com.example.project00.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

	List<Lecture> findByCourseOrderByDateAscTimeAsc(Course course);

	List<Lecture> findByProfessor(Professor professor);

}
