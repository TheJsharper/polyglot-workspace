package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.project00.domain.CreationResource;
import com.example.project00.domain.ResourceCreationQuota;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.ResourceCreationQuotaRepository;
import com.example.project00.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;

class CreationQuotaServiceTest {

	@Test
	void appliesTenPerUserPerResourceOnlyToGoogleAccounts() {
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		ResourceCreationQuotaRepository quotas = mock(ResourceCreationQuotaRepository.class);
		UserAccount googleUser = UserAccount.forStudent(new Student("google-student", "Student", 2026), "hash")
				.withGoogleSubject("google-sub");
		ResourceCreationQuota notes = new ResourceCreationQuota(googleUser, CreationResource.NOTE);
		ResourceCreationQuota lectures = new ResourceCreationQuota(googleUser, CreationResource.LECTURE);
		when(accounts.findByUsernameForUpdate("google-student")).thenReturn(Optional.of(googleUser));
		when(quotas.findForUpdate(googleUser, CreationResource.NOTE)).thenReturn(Optional.of(notes));
		when(quotas.findForUpdate(googleUser, CreationResource.LECTURE)).thenReturn(Optional.of(lectures));
		CreationQuotaService service = new CreationQuotaService(accounts, quotas);

		for (int i = 0; i < 10; i++) {
			service.consumeIfGoogleUser("google-student", CreationResource.NOTE);
		}
		service.consumeIfGoogleUser("google-student", CreationResource.LECTURE);

		assertThat(notes.getCreatedCount()).isEqualTo(10);
		assertThat(lectures.getCreatedCount()).isEqualTo(1);
		assertThatThrownBy(() -> service.consumeIfGoogleUser("google-student", CreationResource.NOTE))
				.isInstanceOf(CreationLimitExceededException.class);
		assertThat(notes.getCreatedCount()).isEqualTo(10);
	}

	@Test
	void doesNotApplyGoogleQuotaToPasswordAccounts() {
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		ResourceCreationQuotaRepository quotas = mock(ResourceCreationQuotaRepository.class);
		UserAccount passwordUser = UserAccount.forStudent(new Student("password-student", "Student", 2026), "hash");
		when(accounts.findByUsernameForUpdate("password-student")).thenReturn(Optional.of(passwordUser));

		new CreationQuotaService(accounts, quotas).consumeIfGoogleUser("password-student", CreationResource.NOTE);

		org.mockito.Mockito.verifyNoInteractions(quotas);
	}
}
