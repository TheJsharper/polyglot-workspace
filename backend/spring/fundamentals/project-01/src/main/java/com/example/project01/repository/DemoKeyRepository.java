package com.example.project01.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.project01.domain.DemoKey;

/** Spring Data repository for {@link DemoKey} entities. */
public interface DemoKeyRepository extends JpaRepository<DemoKey, Long> {

	/**
	 * @param jti the token id, which is also the key id
	 * @return the key, if any
	 */
	Optional<DemoKey> findByJti(String jti);

	/** @return all keys, newest first */
	List<DemoKey> findAllByOrderByCreatedAtDescIdDesc();

}
