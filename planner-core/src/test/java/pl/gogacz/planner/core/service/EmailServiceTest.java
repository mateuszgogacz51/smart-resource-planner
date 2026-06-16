package pl.gogacz.planner.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("sendPasswordResetEmail: powinien wysłać wiadomość na podany adres e-mail")
    void shouldSendEmailToCorrectRecipient() {
        // given
        String email = "jan.kowalski@firma.pl";
        String tempPassword = "Temp@1234";

        // when
        emailService.sendPasswordResetEmail(email, tempPassword);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).contains(email);
    }

    @Test
    @DisplayName("sendPasswordResetEmail: treść maila powinna zawierać tymczasowe hasło")
    void shouldIncludeTemporaryPasswordInEmailBody() {
        // given
        String email = "jan.kowalski@firma.pl";
        String tempPassword = "Temp@1234";

        // when
        emailService.sendPasswordResetEmail(email, tempPassword);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains(tempPassword);
    }

    @Test
    @DisplayName("sendPasswordResetEmail: temat maila powinien zawierać 'Reset Hasła'")
    void shouldSetCorrectEmailSubject() {
        // given
        String email = "jan.kowalski@firma.pl";

        // when
        emailService.sendPasswordResetEmail(email, "anyPassword");

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertThat(captor.getValue().getSubject()).contains("Reset Hasła");
    }

    @Test
    @DisplayName("sendPasswordResetEmail: mailSender.send() powinien zostać wywołany dokładnie raz")
    void shouldCallSendExactlyOnce() {
        // when
        emailService.sendPasswordResetEmail("test@test.pl", "pass");

        // then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
