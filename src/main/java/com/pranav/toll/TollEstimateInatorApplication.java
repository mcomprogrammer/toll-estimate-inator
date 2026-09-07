package com.pranav.toll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TollEstimateInatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TollEstimateInatorApplication.class, args);
	}

}
