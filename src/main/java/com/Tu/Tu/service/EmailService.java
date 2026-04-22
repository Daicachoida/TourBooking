package com.Tu.Tu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String link = "http://localhost:8080/Tu/auth/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Xác nhận email - TourBooking");
        message.setText("Chào bạn,\n\n"
                + "Vui lòng click vào link bên dưới để xác nhận email:\n\n"
                + link + "\n\n"
                + "Link có hiệu lực trong 24 giờ.\n\n"
                + "TourBooking Team");

        mailSender.send(message);
        log.info("Verification email sent to {}", toEmail);
    }
}