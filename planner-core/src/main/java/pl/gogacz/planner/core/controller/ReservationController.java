package pl.gogacz.planner.core.controller;

import org.camunda.bpm.engine.RuntimeService;
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
import pl.gogacz.planner.core.repository.ResourceRepository;
import pl.gogacz.planner.core.service.RuleService;
import pl.gogacz.planner.dto.ReservationRuleContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final CommentRepository commentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ResourceRepository resourceRepository;
    private final RuleService ruleService;

    // 1. Dodajemy "uruchamiacz" procesów z Camundy
    private final RuntimeService runtimeService;

    public ReservationController(ReservationRepository reservationRepository,
                                 CommentRepository commentRepository,
                                 AuditLogRepository auditLogRepository,
                                 ResourceRepository resourceRepository,
                                 RuleService ruleService,
                                 RuntimeService runtimeService) { // Wstrzykujemy w konstruktorze
        this.reservationRepository = reservationRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.resourceRepository = resourceRepository;
        this.ruleService = ruleService;
        this.runtimeService = runtimeService;
    }

    @GetMapping
    public Page<Reservation> getAllReservations(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN") ||
                        a.getAuthority().toUpperCase().contains("EMPLOYEE"));

        String searchTerm = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        if (isPrivileged) {
            return searchTerm != null
                    ? reservationRepository.findAllWithSearch(searchTerm, pageable)
                    : reservationRepository.findAll(pageable);
        } else {
            return searchTerm != null
                    ? reservationRepository.findByUserIdWithSearch(auth.getName(), searchTerm, pageable)
                    : reservationRepository.findByUserId(auth.getName(), pageable);
        }
    }

    @PostMapping
    public Reservation createApplication(@RequestBody Map<String, Object> payload, Authentication auth) {
        Reservation reservation = new Reservation();
        reservation.setUserId(auth.getName());
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);

        Long resId = Long.valueOf(payload.get("resourceId").toString());
        LocalDateTime start = LocalDateTime.parse(payload.get("startTime").toString());
        LocalDateTime end = LocalDateTime.parse(payload.get("endTime").toString());

        Resource resource = resourceRepository.findById(resId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono sprzętu o ID: " + resId));
        reservation.setResource(resource);
        reservation.setStartTime(start);
        reservation.setEndTime(end);

        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(reservation.getStartTime());
        ctx.setEndTime(reservation.getEndTime());
        ruleService.validateReservation(ctx);

        // Jeśli Drools odrzuci wniosek już tutaj, nie odpalamy Camundy
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

        // Zapisujemy poprawny wniosek
        Reservation savedReservation = reservationRepository.save(reservation);

        // === 2. PRZEKAZUJEMY KONTROLĘ DO CAMUNDY ===
        Map<String, Object> variables = new HashMap<>();
        variables.put("reservationId", savedReservation.getId());
        variables.put("resourceId", savedReservation.getResource().getId());

        // Odpalamy proces
        runtimeService.startProcessInstanceByKey("reservationProcess", variables);
        System.out.println("🚀 Wniosek nr " + savedReservation.getId() + " uruchomił proces w Camundzie!");

        return savedReservation;
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

    @PatchMapping("/{id}/unassign")
    public ResponseEntity<Reservation> unassignApplication(@PathVariable Long id, Authentication auth) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setAssignedEmployee(null);
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

    @GetMapping("/stats/statuses")
    public ResponseEntity<Map<String, Long>> getStatusStats() {
        Map<String, Long> stats = reservationRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getStatus().name(),
                        java.util.stream.Collectors.counting()
                ));
        return ResponseEntity.ok(stats);
    }
}