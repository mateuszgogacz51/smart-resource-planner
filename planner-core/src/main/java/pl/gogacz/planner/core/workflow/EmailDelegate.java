package pl.gogacz.planner.core.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.repository.ReservationRepository;
import pl.gogacz.planner.core.repository.UserRepository;

@Component("emailDelegate")
public class EmailDelegate implements JavaDelegate {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender; // Używamy wbudowanego wysyłacza Springa

    public EmailDelegate(ReservationRepository reservationRepository, UserRepository userRepository, JavaMailSender mailSender) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long reservationId = (Long) execution.getVariable("reservationId");
        String decision = (String) execution.getVariable("employeeDecision");

        // 1. Pobieramy wniosek z bazy
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono rezerwacji dla e-maila"));

        // 2. Pobieramy adres e-mail użytkownika
        String userEmail = userRepository.findByUsername(reservation.getUserId())
                .map(user -> user.getEmail())
                .orElse("test@example.com"); // Fallback, jeśli użytkownik nie ma maila

        // 3. Budujemy treść wiadomości w zależności od decyzji
        String subject = "Aktualizacja statusu wniosku nr " + reservationId;
        String text;

        if ("ACCEPTED".equals(decision)) {
            text = "Dobre wieści! Twój wniosek o rezerwację został ZAAKCEPTOWANY przez pracownika. Możesz odebrać sprzęt we wskazanym terminie.";
        } else {
            text = "Niestety, Twój wniosek o rezerwację został ODRZUCONY. Zaloguj się do systemu, aby sprawdzić szczegóły w notatkach.";
        }

        // 4. Wysyłamy e-mail
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("system@smartplanner.pl");
        message.setTo(userEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);

        System.out.println("📧 CAMUNDA (Mailtrap): Pomyślnie wysłano e-mail do " + userEmail + " z decyzją: " + decision);
    }
}