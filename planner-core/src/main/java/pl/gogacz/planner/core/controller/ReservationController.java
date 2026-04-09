package pl.gogacz.planner.core.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.*;
import pl.gogacz.planner.core.repository.AuditLogRepository;
import pl.gogacz.planner.core.repository.CommentRepository;
import pl.gogacz.planner.core.repository.ReservationRepository;
import pl.gogacz.planner.core.repository.ResourceRepository; // DODANY IMPORT
import pl.gogacz.planner.core.service.RuleService;
import pl.gogacz.planner.dto.ReservationRuleContext;

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
    private final ResourceRepository resourceRepository; // DODANE POLE
    private final RuleService ruleService;

    public ReservationController(ReservationRepository reservationRepository,
                                 CommentRepository commentRepository,
                                 AuditLogRepository auditLogRepository,
                                 ResourceRepository resourceRepository, // DODANE DO KONSTRUKTORA
                                 RuleService ruleService) {
        this.reservationRepository = reservationRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.resourceRepository = resourceRepository; // PRZYPISANIE
        this.ruleService = ruleService;
    }

    @GetMapping
    public Page<Reservation> getAllReservations(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN") ||
                        a.getAuthority().toUpperCase().contains("EMPLOYEE"));

        if (isPrivileged) {
            return reservationRepository.findAll(pageable);
        } else {
            return reservationRepository.findByUserId(auth.getName(), pageable);
        }
    }

    @GetMapping("/my")
    public Page<Reservation> getMyApplications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return reservationRepository.findByUserId(auth.getName(), pageable);
    }

    /**
     * POPRAWIONA METODA TWORZENIA:
     * Przyjmuje Mapę, aby wyciągnąć samo resourceId i zamienić je na obiekt Resource
     */
    @PostMapping
    public Reservation createApplication(@RequestBody Map<String, Object> payload, Authentication auth) {
        Reservation reservation = new Reservation();
        reservation.setUserId(auth.getName());
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);

        // 1. Wyciągamy dane z payloadu (Angular wysyła resourceId, startTime, endTime)
        Long resId = Long.valueOf(payload.get("resourceId").toString());
        LocalDateTime start = LocalDateTime.parse(payload.get("startTime").toString());
        LocalDateTime end = LocalDateTime.parse(payload.get("endTime").toString());

        // 2. Pobieramy pełny obiekt Resource z bazy i przypisujemy go
        Resource resource = resourceRepository.findById(resId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono sprzętu o ID: " + resId));
        reservation.setResource(resource);
        reservation.setStartTime(start);
        reservation.setEndTime(end);

        // --- WALIDACJA DROOLS ---
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(reservation.getStartTime());
        ctx.setEndTime(reservation.getEndTime());
        ruleService.validateReservation(ctx);

        if (!ctx.isValid()) {
            reservation.setStatus(ReservationStatus.REJECTED);
            Reservation saved = reservationRepository.save(reservation);

            Comment systemNote = new Comment();
            systemNote.setAuthor("SYSTEM-BOT");
            systemNote.setContent(ctx.getRejectionReason());
            systemNote.setReservation(saved);
            commentRepository.save(systemNote);
            return saved;
        }

        return reservationRepository.save(reservation);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication auth) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

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
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setAssignedEmployee(auth.getName());
        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Reservation> addComment(@PathVariable Long id, @RequestBody Map<String, String> payload, Authentication auth) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        Comment comment = new Comment();
        comment.setContent(payload.get("content"));
        comment.setAuthor(auth.getName());
        comment.setReservation(reservation);
        commentRepository.save(comment);
        return ResponseEntity.ok(reservationRepository.findById(id).get());
    }

    @GetMapping("/{id}/history")
    public List<AuditLog> getHistory(@PathVariable Long id) {
        return auditLogRepository.findByReservationIdOrderByTimestampDesc(id);
    }
}