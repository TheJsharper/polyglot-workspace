package com.example.project00.repository;

import java.util.List;

import com.example.project00.domain.Lecture;
import com.example.project00.domain.Notes;
import com.example.project00.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotesRepository extends JpaRepository<Notes, Long> {

	List<Notes> findByLecture(Lecture lecture);

	List<Notes> findByStudent(Student student);

	List<Notes> findByReviewerIsNull();

}
