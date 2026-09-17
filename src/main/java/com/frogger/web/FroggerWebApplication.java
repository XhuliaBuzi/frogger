package com.frogger.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point. @SpringBootApplication turns on component scanning/auto
 * config for everything under com.frogger.web (config/, game/, ws/).
 * @EnableScheduling is required for GameLoopScheduler's @Scheduled tick()
 * method to actually run - without it, that annotation is silently ignored.
 */
@SpringBootApplication
@EnableScheduling
public class FroggerWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FroggerWebApplication.class, args);
    }
}
