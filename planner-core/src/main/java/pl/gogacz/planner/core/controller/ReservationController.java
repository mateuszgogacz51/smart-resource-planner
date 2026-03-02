package pl.gogacz.planner.core.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.api.model.ResourceDTO;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.ReservationStatus;
import pl.gogacz.planner.core.repository.ReservationRepository;

import java.util.HashMap;
import java.util.List;
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
        // 1. Wyciągamy zalogowanego użytkownika (np. "admin") z kontekstu Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Reservation reservation = new Reservation();
        reservation.setResourceId(resourceDTO.getId());
        reservation.setUserId(currentUsername); // <--- Tutaj zapisujemy faktycznego usera!
        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);

        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("resourceId", resourceDTO.getId());
        processVariables.put("resourceName", resourceDTO.getName());
        processVariables.put("resourceValue", resourceDTO.getValue() != null ? resourceDTO.getValue().doubleValue() : 0.0);
        processVariables.put("reservationId", reservation.getId());
        // Przekazujemy do Camundy informację o tym, kto zaczął proces
        processVariables.put("initiator", currentUsername);

        camundaRuntimeService.startProcessInstanceByKey("Process_Reservation", processVariables);

        return "Sukces! Proces ruszył dla użytkownika: " + currentUsername + ". ID rezerwacji: " + reservation.getId();
    }
}