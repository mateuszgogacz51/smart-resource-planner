package pl.gogacz.planner.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.repository.ReservationRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/applications") // Adres używany przez Angular Service
@CrossOrigin(origins = "http://localhost:4200") // Pozwala na komunikację z Twoim Angularem
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping
    public List<Reservation> getMyApplications(Principal principal) {
        // principal.getName() pobiera username z tokena JWT, który przysyła Angular
        return reservationRepository.findByUserId(principal.getName());
    }

    @PostMapping
    public Reservation createApplication(@RequestBody Reservation reservation, Principal principal) {
        // Automatycznie przypisujemy wniosek do osoby, która go wysyła
        reservation.setUserId(principal.getName());
        return reservationRepository.save(reservation);
    }
}