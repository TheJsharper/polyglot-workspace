package com.example.project00.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.example.project00.domain.DemoKey;
import com.example.project00.security.DemoTokenVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-key-test;DB_CLOSE_DELAY=-1")
@SpringBootTest
class DemoKeyServiceTest {

	@Autowired
	private DemoKeyService service;

	@Autowired
	private DemoTokenVerifier verifier;

	@Test
	void createsIndependentKeysThatVerify() {
		List<DemoKey> created = service.create(3, 5, "  recruiter  ", "manager");

		assertThat(created).hasSize(3);
		assertThat(created).extracting(DemoKey::getJti).doesNotHaveDuplicates();
		assertThat(created).extracting(DemoKey::getPublicKey).doesNotHaveDuplicates();
		assertThat(created).allSatisfy(key -> {
			assertThat(key.getLabel()).isEqualTo("recruiter");
			assertThat(verifier.verify(key.getToken())).isPresent();
		});
	}

	@Test
	void rejectsOutOfRangeArguments() {
		assertThatThrownBy(() -> service.create(0, 5, "x", "m")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.create(11, 5, "x", "m")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.create(1, 0, "x", "m")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.create(1, 31, "x", "m")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.create(1, 5, "x".repeat(101), "m"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void deletedKeyIsRevoked() {
		DemoKey key = service.create(1, 1, "x", "m").get(0);

		service.deleteUnused(key.getId());

		assertThat(verifier.verify(key.getToken())).isEmpty();
		assertThatThrownBy(() -> service.deleteUnused(key.getId())).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void redeemedKeyCannotBeDeleted() {
		DemoKey key = service.create(1, 1, "x", "m").get(0);

		service.recordRedemption(key.getJti());

		assertThatThrownBy(() -> service.deleteUnused(key.getId())).isInstanceOf(IllegalStateException.class);
		assertThat(service.list()).filteredOn(k -> k.getId().equals(key.getId())).singleElement()
			.satisfies(k -> {
				assertThat(k.getRedeemCount()).isEqualTo(1);
				assertThat(k.getRedeemedAt()).isNotNull();
			});
	}

	@Test
	void rejectsTamperedAndForeignTokens() {
		DemoKey a = service.create(1, 1, "a", "m").get(0);
		DemoKey b = service.create(1, 1, "b", "m").get(0);
		String[] parts = a.getToken().split("\\.");
		String[] other = b.getToken().split("\\.");

		assertThat(verifier.verify(parts[0] + "." + parts[1] + "." + other[2])).isEmpty();
		assertThat(verifier.verify(other[0] + "." + parts[1] + "." + parts[2])).isEmpty();
		assertThat(verifier.verify("garbage")).isEmpty();
		assertThat(verifier.verify(null)).isEmpty();
	}

}
