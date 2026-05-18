package com.tripgen.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TripGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripGenApplication.class, args);
    }
}
