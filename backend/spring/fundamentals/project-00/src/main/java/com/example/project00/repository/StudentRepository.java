package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Student} entities.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

	/**
	 * @param studentId the unique student id
	 * @return the student with that id, if any
	 */
	Optional<Student> findByStudentId(String studentId);

}
