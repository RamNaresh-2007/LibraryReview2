package com.library.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CO4: Spring Boot entry point.
 * 
 * @EnableAsync — enables @Async for non-blocking analytics computation.
 * @EnableTransactionManagement — explicit ACID transaction management (CO1).
 */
@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class ManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagementApplication.class, args);
		System.out.println("server is started");
	}
}
