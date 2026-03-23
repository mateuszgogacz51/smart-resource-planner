package pl.gogacz.planner.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/my")
    public List<Reservation> getMyApplications(Authentication auth) {
        return reservationRepository.findByUserId(auth.getName());
    }

    @GetMapping
    public List<Reservation> getAllReservations(Authentication auth) {
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

    // --- ZMIENIONE na PatchMapping (lepsze dla pojedynczych pól) ---
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    // --- NOWE: Przypisanie wniosku do pracownika obsługującego ---
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Reservation> assignApplication(@PathVariable Long id, Authentication auth) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        reservation.setAssignedEmployee(auth.getName()); // Przypisuje osobę, która kliknęła przycisk
        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    // Działające dodawanie komentarzy
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
        // Data utworzy się sama dzięki @PrePersist w Comment.java

        commentRepository.save(comment);
        return ResponseEntity.ok(reservationRepository.findById(id).get());
    }
}