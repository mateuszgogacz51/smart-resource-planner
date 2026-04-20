package pl.gogacz.planner.core.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
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
import pl.gogacz.planner.core.dto.TimelineEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 1. Serwisy Camundy
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public ReservationController(ReservationRepository reservationRepository,
                                 CommentRepository commentRepository,
                                 AuditLogRepository auditLogRepository,
                                 ResourceRepository resourceRepository,
                                 RuleService ruleService,
                                 RuntimeService runtimeService,
                                 TaskService taskService) {
        this.reservationRepository = reservationRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.resourceRepository = resourceRepository;
        this.ruleService = ruleService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
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

        // === PRZEKAZUJEMY KONTROLĘ DO CAMUNDY ===
        Map<String, Object> variables = new HashMap<>();
        variables.put("reservationId", savedReservation.getId());
        variables.put("resourceId", savedReservation.getResource().getId());

        // Odpalamy proces
        runtimeService.startProcessInstanceByKey("reservationProcess", variables);
        System.out.println("🚀 Wniosek nr " + savedReservation.getId() + " uruchomił proces w Camundzie!");

        return savedReservation;
    }

    // === ZMIENIONA METODA: Pchnięcie Camundy po kliknięciu Zatwierdź/Odrzuć ===
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication auth) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono wniosku"));

        String oldStatusStr = reservation.getStatus().toString();
        String newStatusStr = status.toUpperCase();

        // 1. Zapis w naszej bazie
        reservation.setStatus(ReservationStatus.valueOf(newStatusStr));
        Reservation savedReservation = reservationRepository.save(reservation);

        // 2. Audyt
        AuditLog log = new AuditLog();
        log.setReservationId(id);
        log.setChangedBy(auth.getName());
        log.setOldStatus(oldStatusStr);
        log.setNewStatus(newStatusStr);
        auditLogRepository.save(log);

        // 3. CAMUNDA: Zakończenie zadania pracownika
        if ("ACCEPTED".equals(newStatusStr) || "REJECTED".equals(newStatusStr)) {
            Task camundaTask = taskService.createTaskQuery()
                    .processVariableValueEquals("reservationId", id)
                    .singleResult();

            if (camundaTask != null) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("employeeDecision", newStatusStr);

                taskService.complete(camundaTask.getId(), variables);
                System.out.println("🏁 CAMUNDA: Zakończono zadanie! Decyzja: " + newStatusStr);
            }
        }

        return ResponseEntity.ok(savedReservation);
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<Reservation> assignApplication(@PathVariable Long id, Authentication auth) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        String currentUser = auth.getName();

        // Zapis w naszej bazie danych
        reservation.setAssignedEmployee(currentUser);
        Reservation savedReservation = reservationRepository.save(reservation);

        // === MAGIA CAMUNDY: Zablokowanie zadania dla innych ===
        Task camundaTask = taskService.createTaskQuery()
                .processVariableValueEquals("reservationId", id)
                .singleResult();

        if (camundaTask != null) {
            taskService.setAssignee(camundaTask.getId(), currentUser);
            System.out.println("🔧 CAMUNDA: Wniosek nr " + id + " (Zadanie: " + camundaTask.getName() + ") przypisane do pracownika: " + currentUser);
        }

        return ResponseEntity.ok(savedReservation);
    }

    @PatchMapping("/{id}/unassign")
    public ResponseEntity<Reservation> unassignApplication(@PathVariable Long id, Authentication auth) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();

        // Usunięcie pracownika z naszej bazy danych
        reservation.setAssignedEmployee(null);
        Reservation savedReservation = reservationRepository.save(reservation);

        // === MAGIA CAMUNDY: Uwolnienie zadania z powrotem do puli ===
        Task camundaTask = taskService.createTaskQuery()
                .processVariableValueEquals("reservationId", id)
                .singleResult();

        if (camundaTask != null) {
            taskService.setAssignee(camundaTask.getId(), null);
            System.out.println("🔓 CAMUNDA: Wniosek nr " + id + " został uwolniony. Czeka na chętnego!");
        }

        return ResponseEntity.ok(savedReservation);
    }

    // === METODA: Delegowanie ===
    @PatchMapping("/{id}/reassign")
    public ResponseEntity<Reservation> reassignApplication(
            @PathVariable Long id,
            @RequestParam String targetUsername,
            Authentication auth) {

        Reservation reservation = reservationRepository.findById(id).orElseThrow();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().toUpperCase().contains("ADMIN"));

        if (!isAdmin && !auth.getName().equals(reservation.getAssignedEmployee())) {
            throw new RuntimeException("Nie możesz przekazać zadania, które nie należy do ciebie!");
        }

        String previousOwner = reservation.getAssignedEmployee();

        reservation.setAssignedEmployee(targetUsername);
        Reservation savedReservation = reservationRepository.save(reservation);

        Task camundaTask = taskService.createTaskQuery()
                .processVariableValueEquals("reservationId", id)
                .singleResult();

        if (camundaTask != null) {
            taskService.setAssignee(camundaTask.getId(), targetUsername);
        }

        AuditLog log = new AuditLog();
        log.setReservationId(id);
        log.setChangedBy(auth.getName());
        log.setOldStatus("Zadanie: " + previousOwner);
        log.setNewStatus("Przekazane do: " + targetUsername);
        auditLogRepository.save(log);

        System.out.println("🔄 DELEGACJA: Użytkownik " + auth.getName() + " przekazał wniosek nr " + id + " do " + targetUsername);

        return ResponseEntity.ok(savedReservation);
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

    // === NOWA METODA: Zunifikowana Oś Czasu (Timeline) ===
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TimelineEvent>> getUnifiedTimeline(@PathVariable Long id) {
        List<TimelineEvent> timeline = new ArrayList<>();

        // 1. Pobieranie historii statusów i delegacji (Audit Logs)
        auditLogRepository.findByReservationIdOrderByTimestampDesc(id).forEach(log -> {
            String msg = "Zmiana z [" + log.getOldStatus() + "] na [" + log.getNewStatus() + "]";
            timeline.add(new TimelineEvent(
                    log.getTimestamp(),
                    log.getChangedBy(),
                    "AUDIT",
                    msg
            ));
        });

        // 2. Pobieranie komentarzy i notatek
        commentRepository.findByReservationId(id).forEach(comment -> {
            timeline.add(new TimelineEvent(
                    comment.getCreatedAt(),
                    comment.getAuthor(),
                    "COMMENT",
                    comment.getContent()
            ));
        });

        // 3. Sortowanie całości od najnowszego wydarzenia do najstarszego
        timeline.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return ResponseEntity.ok(timeline);
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