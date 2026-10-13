package com.example.project00.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.Hibernate;

/**
 * Common superclass of all entities: provides the database-generated {@code id} and id-based equality.
 * <p>
 * Two entities are equal only when they are of the same (unproxied) class and share a non-null id, so
 * transient entities are only equal to themselves.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Identity is the database id; a constant per-class hash keeps entities stable in sets before and after persisting.
	@Override
	public final boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
			return false;
		}
		return id != null && id.equals(((BaseEntity) other).getId());
	}

	@Override
	public final int hashCode() {
		return Hibernate.getClass(this).hashCode();
	}

}
