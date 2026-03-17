package pl.gogacz.planner.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.ReservationStatus; // KLUCZOWY IMPORT
import pl.gogacz.planner.core.repository.ReservationRepository;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    // 1. Widok dla zwykłego użytkownika (tylko jego wnioski)
    @GetMapping
    public List<Reservation> getMyApplications(Principal principal) {
        return reservationRepository.findByUserId(principal.getName());
    }

    // 2. Widok dla Pracownika i Admina (wszystkie wnioski w systemie)
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // 3. Tworzenie nowego wniosku
    @PostMapping
    public Reservation createApplication(@RequestBody Reservation reservation, Principal principal) {
        reservation.setUserId(principal.getName());
        // Domyślny status przy tworzeniu
        reservation.setStatus(ReservationStatus.PENDING);
        return reservationRepository.save(reservation);
    }

    // 4. Zmiana statusu wniosku (Akceptacja/Odrzucenie)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku o ID: " + id));

        try {
            // Zamiana tekstu z Angulara (np. "ACCEPTED") na wartość Enum
            reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reservationRepository.save(reservation));
    }
}