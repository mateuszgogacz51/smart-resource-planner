package pl.gogacz.planner.core.controller;

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
public class ReservationController {

    private final RuntimeService camundaRuntimeService;
    private final ReservationRepository reservationRepository;

    public ReservationController(RuntimeService camundaRuntimeService, ReservationRepository reservationRepository) {
        this.camundaRuntimeService = camundaRuntimeService;
        this.reservationRepository = reservationRepository;
    }

    @PostMapping("/start")
    public String startReservationProcess(@RequestBody ResourceDTO resourceDTO) {

        // POPRAWKA: Używamy setterów zamiast buildera
        Reservation reservation = new Reservation();
        reservation.setResourceId(resourceDTO.getId());
        reservation.setUserId("current-user");
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation);

        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("resource", resourceDTO);
        processVariables.put("reservationId", reservation.getId());
        processVariables.put("resourceValue", resourceDTO.getValue());

        camundaRuntimeService.startProcessInstanceByKey("Process_Reservation", processVariables);

        return "Proces rezerwacji rozpoczął się pomyślnie! ID Rezerwacji: " + reservation.getId();
    }
}