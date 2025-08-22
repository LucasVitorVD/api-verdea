package com.verdea.api_verdea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ApiVerdeaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiVerdeaApplication.class, args);
	}

}
