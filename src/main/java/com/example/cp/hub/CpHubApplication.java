package com.example.cp.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CpHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(CpHubApplication.class, args);
	}

}
