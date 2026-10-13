package com.example.project00.service;

import java.util.List;

import com.example.project00.domain.Professor;
import com.example.project00.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfessorService {

	private final ProfessorRepository professorRepository;

	public Professor hire(String employeeId, String name, String department) {
		if (professorRepository.findByEmployeeId(employeeId).isPresent()) {
			throw new DuplicateResourceException("Professor", employeeId);
		}
		return professorRepository.save(new Professor(employeeId, name, department));
	}

	@Transactional(readOnly = true)
	public Professor getByEmployeeId(String employeeId) {
		return professorRepository.findByEmployeeId(employeeId)
			.orElseThrow(() -> new ResourceNotFoundException("Professor", employeeId));
	}

	@Transactional(readOnly = true)
	public List<Professor> findAll() {
		return professorRepository.findAll();
	}

	public Professor update(String employeeId, String name, String department) {
		Professor professor = getByEmployeeId(employeeId);
		professor.setName(name);
		professor.setDepartment(department);
		return professor;
	}

	public void delete(String employeeId) {
		Professor professor = getByEmployeeId(employeeId);
		if (!professor.getLectures().isEmpty()) {
			throw new IllegalStateException(
					"Professor " + employeeId + " still delivers " + professor.getLectures().size() + " lecture(s)");
		}
		professorRepository.delete(professor);
	}

}
