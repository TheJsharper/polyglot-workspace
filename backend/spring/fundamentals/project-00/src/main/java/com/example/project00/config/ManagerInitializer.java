package com.example.project00.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.project00.service.AccountService;
import com.example.project00.service.DuplicateResourceException;

/**
 * Creates the single demo-key manager from {@code app.manager.email}. Only active with the {@code dev} profile
 * and when recruiter demo access is enabled; the account is bound to Google on its first sign-in.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class ManagerInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(ManagerInitializer.class);

	private final AccountService accounts;

	public ManagerInitializer(AccountService accounts) {
		this.accounts = accounts;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			accounts.provisionManager().ifPresent(manager -> log.info("Demo-key manager provisioned for Google sign-in"));
			if (accounts.provisionManagerPersonas()) {
				log.info("Manager personas (student, professor, admin) provisioned");
			}
		}
		catch (DuplicateResourceException ex) {
			log.warn("Demo-key manager not provisioned: the configured email or username is already in use");
		}
	}

}
