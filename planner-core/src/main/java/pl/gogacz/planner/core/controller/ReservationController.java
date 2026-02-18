package pl.gogacz.planner.core.controller;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.api.model.ResourceDTO;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.ReservationStatus;
import pl.gogacz.planner.core.repository.ReservationRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final RuntimeService camundaRuntimeService; // Silnik Camunda
    private final ReservationRepository reservationRepository;

    @PostMapping("/start")
    public String startReservationProcess(@RequestBody ResourceDTO resourceDTO) {

        // 1. Zapisz w bazie (Audit log)
        Reservation reservation = Reservation.builder()
                .resourceId(resourceDTO.getId())
                .userId("current-user") // Tu normalnie wziąłbyś z SecurityContext
                .status(ReservationStatus.PENDING)
                .build();

        reservationRepository.save(reservation);

        // 2. Przygotuj mapę zmiennych dla procesu BPMN
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("resource", resourceDTO);
        processVariables.put("reservationId", reservation.getId());
        processVariables.put("resourceValue", resourceDTO.getValue()); // Dla bramki logicznej > 5000

        // 3. Uruchom proces zdefiniowany w pliku reservation.bpmn
        // "Process_Reservation" to ID procesu z XML-a, którego tworzyliśmy wcześniej
        camundaRuntimeService.startProcessInstanceByKey("Process_Reservation", processVariables);

        return "Proces rezerwacji rozpoczęty! ID Rezerwacji: " + reservation.getId();
    }
}