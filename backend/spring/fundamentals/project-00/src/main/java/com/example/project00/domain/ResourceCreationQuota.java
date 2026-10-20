package com.example.project00.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resource_creation_quotas",
		uniqueConstraints = @UniqueConstraint(name = "uk_creation_quota_account_resource",
				columnNames = { "account_id", "resource_type" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResourceCreationQuota extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private UserAccount account;

	@Enumerated(EnumType.STRING)
	@Column(name = "resource_type", nullable = false, length = 20)
	private CreationResource resource;

	@Column(nullable = false)
	private int createdCount;

	public ResourceCreationQuota(UserAccount account, CreationResource resource) {
		this.account = Objects.requireNonNull(account);
		this.resource = Objects.requireNonNull(resource);
	}

	public void consume(int maximum) {
		if (createdCount >= maximum) {
			throw new IllegalStateException("Creation quota reached");
		}
		createdCount++;
	}
}
