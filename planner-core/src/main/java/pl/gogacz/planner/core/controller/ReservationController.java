package pl.gogacz.planner.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Używamy Authentication zamiast Principal
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.Comment;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.ReservationStatus;
import pl.gogacz.planner.core.repository.CommentRepository;
import pl.gogacz.planner.core.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 1. DLA JANA: Pobiera tylko jego wnioski
    @GetMapping("/my")
    public List<Reservation> getMyApplications(Authentication auth) {
        System.out.println("Użytkownik " + auth.getName() + " pobiera swoje wnioski");
        return reservationRepository.findByUserId(auth.getName());
    }

    // 2. DLA ADMINA: Pobiera WSZYSTKO (Usunąłem @PreAuthorize na chwilę, żeby sprawdzić czy to blokuje)
    @GetMapping
    public List<Reservation> getAllReservations(Authentication auth) {
        System.out.println("Konto " + auth.getName() + " z rolami " + auth.getAuthorities() + " pobiera wszystko");

        // Sprawdzamy czy to Admin lub Employee
        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_EMPLOYEE")
                        || a.getAuthority().equals("ADMIN") || a.getAuthority().equals("EMPLOYEE"));

        if (isPrivileged) {
            return reservationRepository.findAll();
        } else {
            return reservationRepository.findByUserId(auth.getName());
        }
    }

    @PostMapping
    public Reservation createApplication(@RequestBody Reservation reservation, Authentication auth) {
        reservation.setUserId(auth.getName());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Reservation> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication auth) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        Comment comment = new Comment();
        comment.setContent(payload.get("content"));
        comment.setAuthor(auth.getName());
        comment.setReservation(reservation);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
        return ResponseEntity.ok(reservationRepository.findById(id).get());
    }
}