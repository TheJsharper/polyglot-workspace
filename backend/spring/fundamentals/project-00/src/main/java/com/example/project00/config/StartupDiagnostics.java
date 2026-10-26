package com.example.project00.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs which profiles and database the application started with, so a missing {@code prod} profile or
 * {@code DB_URL} (which silently falls back to in-memory H2) is visible in the logs. Credentials are never logged.
 */
@Component
public class StartupDiagnostics implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(StartupDiagnostics.class);

	private final Environment environment;

	public StartupDiagnostics(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) {
		String profiles = Arrays.toString(environment.getActiveProfiles());
		String url = environment.getProperty("spring.datasource.url", "unset");
		log.info("Startup: profiles={} database={}", profiles, redact(url));
	}

	/** Keeps only the JDBC scheme and host, dropping query parameters that may hold credentials. */
	static String redact(String url) {
		int query = url.indexOf('?');
		String base = query < 0 ? url : url.substring(0, query);
		return base.replaceAll("//[^/@]*@", "//");
	}

}
