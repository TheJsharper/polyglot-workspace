package com.example.project01;

import java.time.Clock;
import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Demo keys microservice: generation, storage and verification of recruiter demo keys. */
@SpringBootApplication
@EnableAsync
public class Project01Application {

	public static void main(String[] args) {
		SpringApplication.run(Project01Application.class, args);
	}

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	/** Bounded pool so a flood of job requests cannot exhaust the service. */
	@Bean(name = "demoKeyExecutor")
	Executor demoKeyExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("demo-key-job-");
		return executor;
	}

}
