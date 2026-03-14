package com.restaurante.resturante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResturanteApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResturanteApplication.class, args);
	}

}
