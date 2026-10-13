package com.example.project00;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the University API.
 * <p>
 * Swagger UI: {@code /swagger-ui.html}; OpenAPI document: {@code /v3/api-docs}.
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
