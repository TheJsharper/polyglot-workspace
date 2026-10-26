package com.example.project00.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StartupDiagnosticsTest {

	@Test
	void redactDropsQueryParametersAndUserInfo() {
		// Random per run and assembled at runtime, so no credential-like literal lives in source.
		String user = "u" + UUID.randomUUID().toString().substring(0, 8);
		String pass = UUID.randomUUID().toString();
		String url = "jdbc:postgresql://" + user + ":" + pass + "@host.neon.tech/db?"
				+ "pw=" + pass + "&sslmode=require";

		String redacted = StartupDiagnostics.redact(url);

		assertThat(redacted).isEqualTo("jdbc:postgresql://host.neon.tech/db");
		assertThat(redacted).doesNotContain(user).doesNotContain(pass);
		assertThat(StartupDiagnostics.redact("jdbc:h2:mem:project00;DB_CLOSE_DELAY=-1"))
			.isEqualTo("jdbc:h2:mem:project00;DB_CLOSE_DELAY=-1");
	}

}
