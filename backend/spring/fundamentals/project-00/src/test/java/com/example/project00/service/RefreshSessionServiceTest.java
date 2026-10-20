package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.project00.domain.RefreshCredential;
import com.example.project00.repository.RefreshCredentialRepository;
import com.example.project00.security.TokenService;
import com.example.project00.security.TokenService.IssuedToken;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

class RefreshSessionServiceTest {

	@Test
	void rotatesRefreshSecretsAndRevokesFamilyWhenOldSecretIsReused() {
		RefreshCredentialRepository repository = mock(RefreshCredentialRepository.class);
		UserDetailsService users = mock(UserDetailsService.class);
		TokenService tokens = mock(TokenService.class);
		List<RefreshCredential> stored = new ArrayList<>();
		when(repository.save(any(RefreshCredential.class))).thenAnswer(invocation -> {
			RefreshCredential credential = invocation.getArgument(0);
			stored.add(credential);
			return credential;
		});
		when(repository.findByTokenHashForUpdate(anyString())).thenAnswer(invocation ->
				stored.stream().filter(item -> item.getTokenHash().equals(invocation.getArgument(0))).findFirst());
		when(repository.findAllByFamilyId(any(UUID.class))).thenAnswer(invocation ->
				stored.stream().filter(item -> item.getFamilyId().equals(invocation.getArgument(0))).toList());
		when(users.loadUserByUsername("google-account")).thenReturn(
				User.withUsername("google-account").password("not-used").authorities(AuthorityUtils.createAuthorityList("ROLE_STUDENT")).build());
		when(tokens.issue(any())).thenReturn(new IssuedToken("short-access-token", 900));

		RefreshSessionService service = new RefreshSessionService(repository, users, tokens,
				Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC));
		String original = service.issue("google-account");
		RefreshSessionService.RefreshedSession rotated = service.refresh(original);

		assertThat(rotated).isNotNull();
		assertThat(rotated.access()).isEqualTo(new TokenResponse("short-access-token", "Bearer", 900));
		assertThat(rotated.refreshSecret()).isNotEqualTo(original);
		assertThat(service.refresh(original)).isNull();
		assertThat(service.refresh(rotated.refreshSecret())).isNull();
		assertThat(stored).allSatisfy(credential -> assertThat(credential.getRevokedAt()).isNotNull());
	}
}
