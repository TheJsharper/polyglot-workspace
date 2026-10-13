package com.example.project00.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BaseEntityTest {

	@Test
	void sameInstanceIsEqual() {
		Student student = new Student("S-1", "Alan", 2025);

		assertThat(student).isEqualTo(student);
	}

	@Test
	void unsavedEntitiesAreNotEqual() {
		Student first = new Student("S-1", "Alan", 2025);
		Student second = new Student("S-1", "Alan", 2025);

		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void entitiesWithSameIdAndTypeAreEqual() {
		Student first = withId(new Student("S-1", "Alan", 2025), 7L);
		Student second = withId(new Student("S-2", "Grace", 2024), 7L);

		assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
	}

	@Test
	void entitiesWithDifferentIdsAreNotEqual() {
		Student first = withId(new Student("S-1", "Alan", 2025), 7L);
		Student second = withId(new Student("S-1", "Alan", 2025), 8L);

		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void entitiesOfDifferentTypesWithSameIdAreNotEqual() {
		Student student = withId(new Student("X-1", "Alan", 2025), 7L);
		Professor professor = withId(new Professor("X-1", "Alan", "CS"), 7L);

		assertThat(student).isNotEqualTo(professor);
	}

	@Test
	void entityIsNotEqualToNull() {
		assertThat(new Student("S-1", "Alan", 2025)).isNotEqualTo(null);
	}

	@Test
	void hashCodeIsStableWhenIdIsAssigned() {
		Student student = new Student("S-1", "Alan", 2025);
		int before = student.hashCode();

		withId(student, 42L);

		assertThat(student.hashCode()).isEqualTo(before);
	}

	private static <T extends BaseEntity> T withId(T entity, Long id) {
		ReflectionTestUtils.setField(entity, "id", id);
		return entity;
	}

}
