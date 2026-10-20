package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.GoogleLoginTicket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoogleLoginTicketRepository extends JpaRepository<GoogleLoginTicket, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select ticket from GoogleLoginTicket ticket where ticket.tokenHash = :tokenHash")
	Optional<GoogleLoginTicket> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
}
