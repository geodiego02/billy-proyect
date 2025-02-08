package com.billy.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BillyProyectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillyProyectApplication.class, args);
	}

}
