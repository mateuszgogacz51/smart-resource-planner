package pl.gogacz.planner.core.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import pl.gogacz.planner.core.repository.ReservationRepository;
import pl.gogacz.planner.core.model.ReservationStatus;

@Component("updateStatusDelegate")
public class UpdateStatusDelegate implements JavaDelegate {

    private final ReservationRepository reservationRepository;

    public UpdateStatusDelegate(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long reservationId = (Long) execution.getVariable("reservationId");
        String action = (String) execution.getVariable("action"); // Np. "ACCEPT" lub "REJECT"

        reservationRepository.findById(reservationId).ifPresent(res -> {
            if ("ACCEPT".equals(action)) {
                res.setStatus(ReservationStatus.ACCEPTED);
            } else {
                res.setStatus(ReservationStatus.REJECTED);
            }
            reservationRepository.save(res);
        });

        System.out.println("LOG: Camunda zaktualizowała status rezerwacji o ID: " + reservationId);
    }
}