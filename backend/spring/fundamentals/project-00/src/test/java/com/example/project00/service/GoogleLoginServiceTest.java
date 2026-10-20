package com.example.project00.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import com.example.project00.domain.GoogleLoginTicket;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.GoogleLoginTicketRepository;
import com.example.project00.repository.UserAccountRepository;
import com.example.project00.security.TokenService;
import com.example.project00.web.dto.AuthDtos.GoogleOnboardingRequest;
import com.example.project00.web.dto.AuthDtos.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleLoginServiceTest {

	@Test
	void requiresGoogleVerifiedEmail() {
		GoogleLoginService service = service(mock(GoogleLoginTicketRepository.class),
				mock(UserAccountRepository.class), mock(AccountService.class), mock(UserDetailsService.class),
				mock(TokenService.class), mock(RefreshSessionService.class));
		OidcUser identity = mock(OidcUser.class);
		when(identity.getSubject()).thenReturn("google-sub");
		when(identity.getEmail()).thenReturn("person@example.com");
		when(identity.getEmailVerified()).thenReturn(false);

		assertThatThrownBy(() -> service.createTicket(identity)).isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void doesNotAllowGoogleOnboardingToCreateAnAdmin() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(ticket()));
		when(accounts.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
		GoogleLoginService service = service(tickets, accounts, mock(AccountService.class),
				mock(UserDetailsService.class), mock(TokenService.class), mock(RefreshSessionService.class));

		assertThatThrownBy(() -> service.complete("one-time-ticket",
				new GoogleOnboardingRequest(com.example.project00.domain.Role.ADMIN, null, null)))
				.isInstanceOf(IllegalArgumentException.class);
		verify(tickets).findByTokenHashForUpdate(anyString());
	}

	@Test
	void createsGoogleAccountOnlyAfterAllowedRoleSelection() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		AccountService accountService = mock(AccountService.class);
		UserDetailsService users = mock(UserDetailsService.class);
		TokenService tokens = mock(TokenService.class);
		RefreshSessionService refresh = mock(RefreshSessionService.class);
		GoogleLoginTicket ticket = ticket();
		UserAccount account = UserAccount.forStudent(new Student("g-student", "Example Person", 2026), "hash")
				.withGoogleSubject("google-sub")
				.withEmail("person@example.com");
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(ticket));
		when(accounts.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
		when(accountService.registerGoogleAccount("google-sub", "STUDENT", "Example Person", "person@example.com",
				2026, null)).thenReturn(account);
		when(users.loadUserByUsername("g-student")).thenReturn(
				User.withUsername("g-student").password("hash").authorities(AuthorityUtils.createAuthorityList("ROLE_STUDENT")).build());
		when(refresh.issueAccessToken(any())).thenReturn(new TokenResponse("access", "Bearer", 900));
		when(refresh.issue("g-student")).thenReturn("refresh-secret");
		GoogleLoginService service = service(tickets, accounts, accountService, users, tokens, refresh);

		var onboardingRequired = service.complete("one-time-ticket", null);
		assertThat(onboardingRequired.onboardingRequired()).isTrue();
		assertThat(ticket.isConsumed()).isFalse();

		var result = service.complete("one-time-ticket",
				new GoogleOnboardingRequest(com.example.project00.domain.Role.STUDENT, 2026, null));
		assertThat(result.session()).isEqualTo(new TokenResponse("access", "Bearer", 900));
		assertThat(result.refreshSecret()).isEqualTo("refresh-secret");
		assertThat(result.newAccount()).isTrue();
		assertThat(ticket.isConsumed()).isTrue();
		verify(accountService).registerGoogleAccount("google-sub", "STUDENT", "Example Person", "person@example.com",
				2026, null);
	}

	private static GoogleLoginService service(GoogleLoginTicketRepository tickets, UserAccountRepository accounts,
			AccountService accountService, UserDetailsService users, TokenService tokens,
			RefreshSessionService refresh) {
		return new GoogleLoginService(tickets, accounts, accountService, users, refresh,
				Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC));
	}

	private static GoogleLoginTicket ticket() {
		return new GoogleLoginTicket("hashed-ticket", "google-sub", "person@example.com", "Example Person",
				Instant.parse("2026-10-20T11:00:00Z"));
	}
}
