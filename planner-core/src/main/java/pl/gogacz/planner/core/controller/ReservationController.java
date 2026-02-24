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
        Reservation reservation = new Reservation();
        reservation.setResourceId(resourceDTO.getId());
        reservation.setUserId("current-user");
        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);

        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("resourceId", resourceDTO.getId());
        processVariables.put("resourceName", resourceDTO.getName());

        if (resourceDTO.getValue() != null) {
            processVariables.put("resourceValue", resourceDTO.getValue().doubleValue());
        } else {
            processVariables.put("resourceValue", 0.0);
        }

        processVariables.put("reservationId", reservation.getId());

        camundaRuntimeService.startProcessInstanceByKey("Process_Reservation", processVariables);

        return "Sukces! Proces ruszył. ID: " + reservation.getId();
    }
}