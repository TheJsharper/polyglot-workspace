package com.example.project00.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.project00.domain.RefreshCredential;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshCredentialRepository extends JpaRepository<RefreshCredential, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select credential from RefreshCredential credential where credential.tokenHash = :tokenHash")
	Optional<RefreshCredential> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

	List<RefreshCredential> findAllByFamilyId(UUID familyId);
}
