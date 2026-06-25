package com.music.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages = "com.music" so shared beans in common/ (GlobalExceptionHandler,
// CurrentUserProvider, JwtUtil) are component-scanned alongside this service.
@SpringBootApplication(scanBasePackages = "com.music")
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
