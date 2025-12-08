package com.company.holiday.holiday_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HolidayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HolidayServiceApplication.class, args);
	}

}
