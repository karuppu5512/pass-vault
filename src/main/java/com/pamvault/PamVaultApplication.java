package com.pamvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PamVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(PamVaultApplication.class, args);
    }
}
