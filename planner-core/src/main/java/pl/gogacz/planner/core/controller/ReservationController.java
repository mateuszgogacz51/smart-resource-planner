package pl.gogacz.planner.core.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.AuditLog;
import pl.gogacz.planner.core.model.Comment;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.ReservationStatus;
import pl.gogacz.planner.core.repository.AuditLogRepository;
import pl.gogacz.planner.core.repository.CommentRepository;
import pl.gogacz.planner.core.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final CommentRepository commentRepository;
    private final AuditLogRepository auditLogRepository;

    // Konstruktor z wstrzykiwaniem wszystkich potrzebnych repozytoriów
    public ReservationController(ReservationRepository reservationRepository,
                                 CommentRepository commentRepository,
                                 AuditLogRepository auditLogRepository) {
        this.reservationRepository = reservationRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Pobiera wnioski zalogowanego użytkownika (Paginacja)
     */
    @GetMapping("/my")
    public Page<Reservation> getMyApplications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return reservationRepository.findByUserId(auth.getName(), pageable);
    }

    /**
     * Pobiera wszystkie wnioski (Admin/Pracownik) lub własne (User) z paginacją
     */
    @GetMapping
    public Page<Reservation> getAllReservations(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().replace("ROLE_", "").equals("ADMIN") ||
                        a.getAuthority().replace("ROLE_", "").equals("EMPLOYEE"));

        if (isPrivileged) {
            return reservationRepository.findAll(pageable);
        } else {
            return reservationRepository.findByUserId(auth.getName(), pageable);
        }
    }

    @PostMapping
    public Reservation createApplication(@RequestBody Reservation reservation, Authentication auth) {
        reservation.setUserId(auth.getName());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    /**
     * Aktualizacja statusu wraz z zapisem do Audit Log (Historii)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication auth) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku o ID: " + id));

        // Sprawdzenie uprawnień
        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("EMPLOYEE"));

        if (!isPrivileged) {
            throw new RuntimeException("Brak uprawnień do zmiany statusu.");
        }

        // ZAPIS DO HISTORII (Audit Log)
        AuditLog log = new AuditLog();
        log.setReservationId(id);
        log.setChangedBy(auth.getName());
        log.setOldStatus(reservation.getStatus().toString());
        log.setNewStatus(status.toUpperCase());
        auditLogRepository.save(log);

        reservation.setStatus(ReservationStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Reservation> assignApplication(@PathVariable Long id, Authentication auth) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        reservation.setAssignedEmployee(auth.getName());
        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Reservation> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication auth) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        boolean isOwner = reservation.getUserId().equals(auth.getName());
        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("EMPLOYEE"));

        if (!isOwner && !isPrivileged) {
            throw new RuntimeException("Nie możesz komentować cudzych wniosków!");
        }

        Comment comment = new Comment();
        comment.setContent(payload.get("content"));
        comment.setAuthor(auth.getName());
        comment.setReservation(reservation);
        commentRepository.save(comment);

        return ResponseEntity.ok(reservationRepository.findById(id).get());
    }

    /**
     * Pobieranie historii zmian dla konkretnego wniosku
     */
    @GetMapping("/{id}/history")
    public List<AuditLog> getHistory(@PathVariable Long id) {
        return auditLogRepository.findByReservationIdOrderByTimestampDesc(id);
    }
}