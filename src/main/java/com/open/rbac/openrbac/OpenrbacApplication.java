package com.open.rbac.openrbac;

import org.springframework.retry.annotation.EnableRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;

@Configuration
@EnableRetry
@EnableCaching
@SpringBootApplication
public class OpenrbacApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenrbacApplication.class, args);
	}

}
