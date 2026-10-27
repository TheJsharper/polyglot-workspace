package com.example.project00.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.util.Optional;

import com.example.project00.domain.Professor;
import com.example.project00.domain.Student;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsServiceTest {

	@Mock
	private UserAccountRepository userAccountRepository;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

	private AccountUserDetailsService service;

	@BeforeEach
	void setUp() {
		AppSecurityProperties properties = new AppSecurityProperties(
				new AppSecurityProperties.Admin("admin", "admin-pass"),
				new AppSecurityProperties.Jwt("x".repeat(32), Duration.ofHours(1), "issuer"));
		service = new AccountUserDetailsService(userAccountRepository, properties, passwordEncoder,
				java.time.Clock.systemUTC());
	}

	@Test
	void loadsConfiguredAdminWithHashedPassword() {
		UserDetails admin = service.loadUserByUsername("admin");

		assertThat(admin.getUsername()).isEqualTo("admin");
		assertThat(admin.getPassword()).isNotEqualTo("admin-pass");
		assertThat(passwordEncoder.matches("admin-pass", admin.getPassword())).isTrue();
		assertThat(AuthorityUtils.authorityListToSet(admin.getAuthorities())).containsExactly("ROLE_ADMIN");
	}

	@Test
	void loadsStudentAccount() {
		given(userAccountRepository.findByUsername("S-1"))
			.willReturn(Optional.of(UserAccount.forStudent(new Student("S-1", "Alan", 2025), "hash")));

		UserDetails user = service.loadUserByUsername("S-1");

		assertThat(user.getPassword()).isEqualTo("hash");
		assertThat(AuthorityUtils.authorityListToSet(user.getAuthorities())).containsExactly("ROLE_STUDENT");
	}

	@Test
	void loadsProfessorAccount() {
		given(userAccountRepository.findByUsername("E-1"))
			.willReturn(Optional.of(UserAccount.forProfessor(new Professor("E-1", "Ada", "CS"), "hash")));

		assertThat(AuthorityUtils.authorityListToSet(service.loadUserByUsername("E-1").getAuthorities()))
			.containsExactly("ROLE_PROFESSOR");
	}

	@Test
	void unknownUserIsRejected() {
		given(userAccountRepository.findByUsername("ghost")).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.loadUserByUsername("ghost")).isInstanceOf(UsernameNotFoundException.class);
	}

}
