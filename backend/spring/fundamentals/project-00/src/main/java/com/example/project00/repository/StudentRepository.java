package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findByStudentId(String studentId);

}
