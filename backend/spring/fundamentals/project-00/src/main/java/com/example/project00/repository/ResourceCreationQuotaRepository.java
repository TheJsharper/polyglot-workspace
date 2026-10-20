package com.example.project00.repository;

import java.util.Optional;

import com.example.project00.domain.CreationResource;
import com.example.project00.domain.ResourceCreationQuota;
import com.example.project00.domain.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceCreationQuotaRepository extends JpaRepository<ResourceCreationQuota, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select quota from ResourceCreationQuota quota where quota.account = :account and quota.resource = :resource")
	Optional<ResourceCreationQuota> findForUpdate(@Param("account") UserAccount account,
			@Param("resource") CreationResource resource);
}
