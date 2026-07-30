package com.ahmadisyraf39.spabs_v2.common.email;

import com.ahmadisyraf39.spabs_v2.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.password-reset.token-expiry-minutes}")
    private long tokenExpiryMinutes;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendWelcomeEmail(User user, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Welcome to Spabs Academy");
        message.setText(
                """
                Hi %s,

                An account has been created for you at Spabs Academy.

                Email: %s
                Temporary password: %s

                You'll be asked to set your own password the first time you log in.
                """
                        .formatted(user.getFullName(), user.getEmail(), temporaryPassword));
        send(message);
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Password reset request");
        message.setText(
                """
                Hi %s,

                We received a request to reset your Spabs Academy password. Use the link below \
                within the next %d minutes:

                %s/reset-password?token=%s

                If you didn't request this, you can safely ignore this email.
                """
                        .formatted(user.getFullName(), tokenExpiryMinutes, frontendBaseUrl, resetToken));
        send(message);
    }

    private void send(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Failed to send email to {}: {}", message.getTo(), e.getMessage());
        }
    }
}
