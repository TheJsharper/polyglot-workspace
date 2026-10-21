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
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class GoogleLoginServiceTest {

	private static final String PICTURE = "https://lh3.googleusercontent.com/a/example-avatar=s96-c";

	private static final String BINDING = "browser-binding";

	@Test
	void requiresGoogleVerifiedEmail() {
		GoogleLoginService service = service(mock(GoogleLoginTicketRepository.class),
				mock(UserAccountRepository.class), mock(AccountService.class), mock(UserDetailsService.class),
				mock(TokenService.class), mock(RefreshSessionService.class));
		OidcUser identity = mock(OidcUser.class);
		when(identity.getSubject()).thenReturn("google-sub");
		when(identity.getEmail()).thenReturn("person@example.com");
		when(identity.getEmailVerified()).thenReturn(false);

		assertThatThrownBy(() -> service.createTicket(identity, BINDING)).isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void doesNotAllowGoogleOnboardingToCreateAnAdmin() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(ticket()));
		when(accounts.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
		GoogleLoginService service = service(tickets, accounts, mock(AccountService.class),
				mock(UserDetailsService.class), mock(TokenService.class), mock(RefreshSessionService.class));

		assertThatThrownBy(() -> service.complete("one-time-ticket", BINDING,
				new GoogleOnboardingRequest(com.example.project00.domain.Role.ADMIN, null, null)))
				.isInstanceOf(IllegalArgumentException.class);
		verify(tickets).findByTokenHashForUpdate(anyString());
	}

	@Test
	void linksTheConfiguredManagerOnFirstGoogleSignInWithoutOnboarding() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		AccountService accountService = mock(AccountService.class);
		UserDetailsService users = mock(UserDetailsService.class);
		RefreshSessionService refresh = mock(RefreshSessionService.class);
		GoogleLoginTicket ticket = ticket();
		UserAccount manager = UserAccount.forManager("manager", "hash").withEmail("person@example.com");
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(ticket));
		when(accounts.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
		when(accountService.linkManagerGoogleAccount("google-sub", "person@example.com"))
			.thenReturn(Optional.of(manager));
		when(users.loadUserByUsername("manager")).thenReturn(User.withUsername("manager")
			.password("hash")
			.authorities(AuthorityUtils.createAuthorityList("ROLE_MANAGER"))
			.build());
		when(refresh.issueAccessToken(any())).thenReturn(new TokenResponse("access", "Bearer", 900));
		when(refresh.issue("manager")).thenReturn("refresh-secret");
		GoogleLoginService service = service(tickets, accounts, accountService, users, mock(TokenService.class),
				refresh);

		var result = service.complete("one-time-ticket", BINDING, null);

		assertThat(result.onboardingRequired()).isFalse();
		assertThat(result.newAccount()).isFalse();
		assertThat(result.newAccount()).isFalse();
		assertThat(result.session()).isEqualTo(new TokenResponse("access", "Bearer", 900));
		assertThat(ticket.isConsumed()).isTrue();
		verify(accountService, never()).registerGoogleAccount(anyString(), anyString(), anyString(), anyString(),
				org.mockito.ArgumentMatchers.anyInt(), any());
	}

	@Test
	void doesNotAllowGoogleOnboardingToCreateAManager() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		UserAccountRepository accounts = mock(UserAccountRepository.class);
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(ticket()));
		when(accounts.findByGoogleSubject("google-sub")).thenReturn(Optional.empty());
		GoogleLoginService service = service(tickets, accounts, mock(AccountService.class),
				mock(UserDetailsService.class), mock(TokenService.class), mock(RefreshSessionService.class));

		assertThatThrownBy(() -> service.complete("one-time-ticket", BINDING,
				new GoogleOnboardingRequest(com.example.project00.domain.Role.MANAGER, null, null)))
				.isInstanceOf(IllegalArgumentException.class);
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

		var onboardingRequired = service.complete("one-time-ticket", BINDING, null);
		assertThat(onboardingRequired.onboardingRequired()).isTrue();
		assertThat(ticket.isConsumed()).isFalse();

		var result = service.complete("one-time-ticket", BINDING,
				new GoogleOnboardingRequest(com.example.project00.domain.Role.STUDENT, 2026, null));
		assertThat(result.session()).isEqualTo(new TokenResponse("access", "Bearer", 900));
		assertThat(result.refreshSecret()).isEqualTo("refresh-secret");
		assertThat(result.newAccount()).isTrue();
		assertThat(ticket.isConsumed()).isTrue();
		assertThat(account.getDisplayName()).isEqualTo("Example Person");
		assertThat(account.getPictureUrl()).isEqualTo(PICTURE);
		assertThat(account.resolveDisplayName()).isEqualTo("Example Person");
		verify(accountService).registerGoogleAccount("google-sub", "STUDENT", "Example Person", "person@example.com",
				2026, null);
	}

	@Test
	void capturesOnlyGoogleHostedHttpsPictures() {
		assertThat(GoogleLoginService.safePictureUrl(PICTURE)).isEqualTo(PICTURE);
		assertThat(GoogleLoginService.safePictureUrl("https://googleusercontent.com/a/x")).isNotNull();
		assertThat(GoogleLoginService.safePictureUrl(null)).isNull();
		assertThat(GoogleLoginService.safePictureUrl("http://lh3.googleusercontent.com/a/x")).isNull();
		assertThat(GoogleLoginService.safePictureUrl("https://evil.example/a.png")).isNull();
		assertThat(GoogleLoginService.safePictureUrl("https://googleusercontent.com.evil.example/a")).isNull();
		assertThat(GoogleLoginService.safePictureUrl("https://user@lh3.googleusercontent.com/a")).isNull();
		assertThat(GoogleLoginService.safePictureUrl("javascript:alert(1)")).isNull();
		assertThat(GoogleLoginService.safePictureUrl("https://lh3.googleusercontent.com/" + "a".repeat(2048)))
			.isNull();
	}

	@Test
	void storesSafePictureOnLoginTicket() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		GoogleLoginService service = service(tickets, mock(UserAccountRepository.class), mock(AccountService.class),
				mock(UserDetailsService.class), mock(TokenService.class), mock(RefreshSessionService.class));
		OidcUser identity = mock(OidcUser.class);
		when(identity.getSubject()).thenReturn("google-sub");
		when(identity.getEmail()).thenReturn("Person@Example.com");
		when(identity.getEmailVerified()).thenReturn(true);
		when(identity.getFullName()).thenReturn("  Example Person ");
		when(identity.getPicture()).thenReturn(PICTURE);

		service.createTicket(identity, "browser-binding");

		ArgumentCaptor<GoogleLoginTicket> saved = ArgumentCaptor.forClass(GoogleLoginTicket.class);
		verify(tickets).save(saved.capture());
		assertThat(saved.getValue().getBindingHash()).isEqualTo(GoogleLoginService.hash("browser-binding"));
		assertThat(saved.getValue().getDisplayName()).isEqualTo("Example Person");
		assertThat(saved.getValue().getPictureUrl()).isEqualTo(PICTURE);
		assertThat(saved.getValue().getEmail()).isEqualTo("person@example.com");
	}

	private static GoogleLoginService service(GoogleLoginTicketRepository tickets, UserAccountRepository accounts,
			AccountService accountService, UserDetailsService users, TokenService tokens,
			RefreshSessionService refresh) {
		return new GoogleLoginService(tickets, accounts, accountService, users, refresh,
				Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC));
	}

	private static GoogleLoginTicket ticket() {
		return new GoogleLoginTicket("hashed-ticket", "google-sub", "person@example.com", "Example Person", PICTURE,
				GoogleLoginService.hash(BINDING), Instant.parse("2026-10-20T11:00:00Z"));
	}

	@Test
	void rejectsCompletionFromABrowserThatDidNotStartTheFlow() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		GoogleLoginTicket ticket = ticket();
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(ticket));
		GoogleLoginService service = service(tickets, mock(UserAccountRepository.class), mock(AccountService.class),
				mock(UserDetailsService.class), mock(TokenService.class), mock(RefreshSessionService.class));

		for (String binding : new String[] { null, "", "someone-elses-binding" }) {
			assertThatThrownBy(() -> service.complete("one-time-ticket", binding, null))
				.isInstanceOf(BadCredentialsException.class);
		}
		assertThat(ticket.isConsumed()).isFalse();
	}

	@Test
	void rejectsTicketsWithoutABinding() {
		GoogleLoginTicketRepository tickets = mock(GoogleLoginTicketRepository.class);
		when(tickets.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(new GoogleLoginTicket(
				"hashed-ticket", "google-sub", "person@example.com", "Example Person", PICTURE,
				Instant.parse("2026-10-20T11:00:00Z"))));
		GoogleLoginService service = service(tickets, mock(UserAccountRepository.class), mock(AccountService.class),
				mock(UserDetailsService.class), mock(TokenService.class), mock(RefreshSessionService.class));

		assertThatThrownBy(() -> service.complete("one-time-ticket", BINDING, null))
			.isInstanceOf(BadCredentialsException.class);
	}
}
