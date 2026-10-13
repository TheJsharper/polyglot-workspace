package com.example.project00.security;

import com.example.project00.domain.Role;
import com.example.project00.repository.UserAccountRepository;
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

	AccountUserDetailsService(UserAccountRepository userAccountRepository, AppSecurityProperties properties,
			PasswordEncoder passwordEncoder) {
		this.userAccountRepository = userAccountRepository;
		this.admin = User.withUsername(properties.admin().username())
			.password(passwordEncoder.encode(properties.admin().password()))
			.roles(Role.ADMIN.name())
			.build();
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		if (admin.getUsername().equals(username)) {
			return User.withUserDetails(admin).build();
		}
		return userAccountRepository.findByUsername(username)
			.map(account -> User.withUsername(account.getUsername())
				.password(account.getPasswordHash())
				.roles(account.getRole().name())
				.build())
			.orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
	}

}
