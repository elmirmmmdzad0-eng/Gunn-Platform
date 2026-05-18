package com.tripgen.api;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String username, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("TripGen email verification");
        message.setText("""
                Salam %s,

                TripGen hesabini aktiv etmek ucun asagidaki linke daxil olun:
                %s

                Eger bu qeydiyyati siz etmemisinizse, bu emaili nezere almayin.
                """.formatted(username, verificationLink));

        mailSender.send(message);
    }
}
