package com.passvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PassVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(PassVaultApplication.class, args);
    }
}
