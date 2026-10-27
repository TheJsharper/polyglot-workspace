package com.example.project00.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.example.project00.domain.Role;
import com.example.project00.domain.UserAccount;
import com.example.project00.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads login credentials for password authentication: the configured admin first, then the student and
 * professor accounts stored in the database.
 */
@Service
class AccountUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;

	private final UserDetails admin;

	private final Clock clock;

	AccountUserDetailsService(UserAccountRepository userAccountRepository, AppSecurityProperties properties,
			PasswordEncoder passwordEncoder, Clock clock) {
		this.userAccountRepository = userAccountRepository;
		this.clock = clock;
		this.admin = User.withUsername(properties.admin().username())
			.password(passwordEncoder.encode(properties.admin().password()))
			.roles(Role.ADMIN.name())
			.build();
	}

	/**
	 * Expired demo logins are reported as unknown users, so login fails with the generic message and refresh
	 * sessions are revoked.
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		if (admin.getUsername().equals(username)) {
			return User.withUserDetails(admin).build();
		}
		Instant now = clock.instant();
		return userAccountRepository.findByUsername(username)
			.or(() -> username != null && username.contains("@")
					? userAccountRepository.findByEmail(username.trim().toLowerCase(Locale.ROOT))
					: Optional.empty())
			.filter(account -> !account.isDemoExpired(now))
			.map(account -> User.withUsername(account.getUsername())
				.password(account.getPasswordHash())
				.authorities(authorities(account))
				.build())
			.orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
	}

	private static List<SimpleGrantedAuthority> authorities(UserAccount account) {
		SimpleGrantedAuthority role = new SimpleGrantedAuthority(account.getRole().authority());
		return account.isDemo() ? List.of(role, new SimpleGrantedAuthority(TokenService.DEMO_ACCOUNT_AUTHORITY))
				: List.of(role);
	}

}
