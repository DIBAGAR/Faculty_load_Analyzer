package com.example.facultyload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FacultyloadApplication {

	public static void main(String[] args) {
		SpringApplication.run(FacultyloadApplication.class, args);
	}

}
