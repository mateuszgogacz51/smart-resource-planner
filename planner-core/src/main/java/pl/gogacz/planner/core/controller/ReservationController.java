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
    private final RuleService ruleService;

    public ReservationController(ReservationRepository reservationRepository,
                                 CommentRepository commentRepository,
                                 AuditLogRepository auditLogRepository,
                                 RuleService ruleService) {
        this.reservationRepository = reservationRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
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

    @PostMapping
    public Reservation createApplication(@RequestBody Reservation reservation, Authentication auth) {
        reservation.setUserId(auth.getName());
        reservation.setCreatedAt(LocalDateTime.now());

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

        reservation.setStatus(ReservationStatus.PENDING);
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