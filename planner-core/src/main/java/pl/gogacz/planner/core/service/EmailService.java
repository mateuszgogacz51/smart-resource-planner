package pl.gogacz.planner.core.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("admin@smart-planner.com"); // Mailtrap i tak to nadpisze, ale dobrze mieć to dla porządku
        message.setTo(toEmail);
        message.setSubject("Smart Resource Planner - Reset Hasła");
        message.setText(
                "Witaj,\n\n" +
                        "Administrator systemu zresetował Twoje hasło dostępu.\n\n" +
                        "Twoje nowe, tymczasowe hasło to: " + temporaryPassword + "\n\n" +
                        "Ze względów bezpieczeństwa prosimy o zmianę hasła zaraz po zalogowaniu.\n\n" +
                        "Pozdrawiamy,\n" +
                        "System Smart Resource Planner 🚀"
        );

        mailSender.send(message);
    }
}