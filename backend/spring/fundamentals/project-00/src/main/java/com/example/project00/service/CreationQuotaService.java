package com.example.project00.service;

import com.example.project00.domain.CreationResource;
import com.example.project00.domain.ResourceCreationQuota;
import com.example.project00.repository.ResourceCreationQuotaRepository;
import com.example.project00.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreationQuotaService {

	private static final int MAX_CREATIONS_PER_RESOURCE = 10;

	private final UserAccountRepository accounts;
	private final ResourceCreationQuotaRepository quotas;

	public CreationQuotaService(UserAccountRepository accounts, ResourceCreationQuotaRepository quotas) {
		this.accounts = accounts;
		this.quotas = quotas;
	}

	@Transactional
	public void consumeIfGoogleUser(String username, CreationResource resource) {
		var account = accounts.findByUsernameForUpdate(username).orElse(null);
		if (account == null || account.getGoogleSubject() == null) {
			return;
		}
		ResourceCreationQuota quota = quotas.findForUpdate(account, resource)
			.orElseGet(() -> quotas.save(new ResourceCreationQuota(account, resource)));
		if (quota.getCreatedCount() >= MAX_CREATIONS_PER_RESOURCE) {
			throw new CreationLimitExceededException(resource.name());
		}
		quota.consume(MAX_CREATIONS_PER_RESOURCE);
	}
}
