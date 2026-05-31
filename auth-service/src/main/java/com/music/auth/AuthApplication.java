package com.music.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages = "com.music" so shared beans in common/ (GlobalExceptionHandler,
// CurrentUserProvider, JwtUtil) are component-scanned alongside this service.
@SpringBootApplication(scanBasePackages = "com.music")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
