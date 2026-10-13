package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Professor} entities.
 */
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

	/**
	 * @param employeeId the unique employee id
	 * @return the professor with that id, if any
	 */
	Optional<Professor> findByEmployeeId(String employeeId);

}
