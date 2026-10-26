package com.example.project00.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StartupDiagnosticsTest {

	@Test
	void redactDropsQueryParametersAndUserInfo() {
		assertThat(StartupDiagnostics.redact("jdbc:postgresql://user:secret@host.neon.tech/db?password=x&sslmode=require"))
			.isEqualTo("jdbc:postgresql://host.neon.tech/db");
		assertThat(StartupDiagnostics.redact("jdbc:h2:mem:project00;DB_CLOSE_DELAY=-1"))
			.isEqualTo("jdbc:h2:mem:project00;DB_CLOSE_DELAY=-1");
	}

}
