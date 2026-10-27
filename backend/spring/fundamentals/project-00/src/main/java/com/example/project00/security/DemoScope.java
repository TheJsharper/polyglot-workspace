package com.example.project00.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Decides which dataset (real or demo) the current caller may see. Generated recruiter demo logins only see demo
 * records, ordinary users only see real records, and real admins/managers see both.
 */
@Component("demoScope")
public class DemoScope {

	/** @return whether the current caller is a generated recruiter demo login */
	public boolean isDemoCaller() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication instanceof JwtAuthenticationToken jwt && isDemoAccount(jwt);
	}

	/**
	 * @param entityDemo whether the record belongs to the demo dataset
	 * @return whether the current caller may see the record; calls without authentication (startup, system code)
	 *     see everything
	 */
	public boolean canSee(boolean entityDemo) {
		return visible(entityDemo);
	}

	/** Static form of {@link #canSee(boolean)} for services. */
	public static boolean visible(boolean entityDemo) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return true;
		}
		if (authentication instanceof JwtAuthenticationToken jwt && isDemoAccount(jwt)) {
			return entityDemo;
		}
		if (hasAuthority(authentication, "ROLE_ADMIN") || hasAuthority(authentication, "ROLE_MANAGER")) {
			return true;
		}
		return !entityDemo;
	}

	static boolean isDemoAccount(JwtAuthenticationToken jwt) {
		return Boolean.TRUE.equals(jwt.getToken().getClaims().get(TokenService.DEMO_ACCOUNT_CLAIM));
	}

	private static boolean hasAuthority(Authentication authentication, String authority) {
		for (GrantedAuthority granted : authentication.getAuthorities()) {
			if (authority.equals(granted.getAuthority())) {
				return true;
			}
		}
		return false;
	}

}
