package com.music.notification.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String TEMPLATE_PATH = "templates/reset-password-email.html";

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String from;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    private String template;

    @PostConstruct
    void loadTemplate() throws IOException {
        try (InputStream in = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void sendResetPasswordEmail(String email, String token) {
        String resetLink = resetPasswordUrl + "?token=" + token;
        String html = template.replace("{{resetLink}}", resetLink);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Đặt lại mật khẩu SoundClown");
            helper.setText(html, true); // true = HTML
            mailSender.send(message);
            log.info("Sent reset-password email to {}", email);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to build reset-password email", e);
        }
    }
}
