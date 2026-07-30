package com.ahmadisyraf39.spabs_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpabsV2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpabsV2Application.class, args);
	}

}
