package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

	Optional<Professor> findByEmployeeId(String employeeId);

}
