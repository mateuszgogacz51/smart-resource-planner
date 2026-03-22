package pl.gogacz.planner.core.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import pl.gogacz.planner.core.model.ReservationStatus;
import pl.gogacz.planner.core.repository.ReservationRepository;

@Component
public class UpdateReservationStatusDelegate implements JavaDelegate {

    private final ReservationRepository reservationRepository;

    public UpdateReservationStatusDelegate(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // 1. Pobieramy zmienne z procesu Camundy
        Long reservationId = (Long) execution.getVariable("reservationId");
        Boolean isApproved = (Boolean) execution.getVariable("droolsApproval");

        // 2. Szukamy wniosku w PostgreSQL i aktualizujemy jego status
        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            if (Boolean.TRUE.equals(isApproved)) {
                // TUTAJ BYŁ BŁĄD: Zmieniono APPROVED na ACCEPTED
                reservation.setStatus(ReservationStatus.ACCEPTED);
            } else {
                reservation.setStatus(ReservationStatus.REJECTED);
            }
            reservationRepository.save(reservation);

            // Log do konsoli
            System.out.println(">>> SUKCES: Zaktualizowano w bazie status rezerwacji ID: "
                    + reservationId + " na " + reservation.getStatus() + " <<<");
        });
    }
}