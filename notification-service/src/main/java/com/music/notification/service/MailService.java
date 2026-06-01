package com.music.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail-from}")
    private String from;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    public void sendResetPasswordEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Đặt lại mật khẩu SoundClown");
        message.setText("""
                Bạn vừa yêu cầu đặt lại mật khẩu.
                Nhấn vào liên kết sau (có hiệu lực trong 30 phút):
                %s?token=%s

                Nếu không phải bạn, hãy bỏ qua email này.""".formatted(resetPasswordUrl, token));
        mailSender.send(message);
        log.info("Sent reset-password email to {}", email);
    }
}
