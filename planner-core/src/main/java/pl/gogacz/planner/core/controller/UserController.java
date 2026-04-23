package pl.gogacz.planner.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.repository.ReservationRepository;
import pl.gogacz.planner.core.repository.UserRepository;
import pl.gogacz.planner.core.service.EmailService;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final EmailService emailService;

    public UserController(UserRepository userRepository, ReservationRepository reservationRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<User> changeUserRole(@PathVariable Long id, @RequestParam String newRole) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        String roleWithPrefix = newRole.startsWith("ROLE_") ? newRole : "ROLE_" + newRole;
        user.setRoles(List.of(roleWithPrefix.toUpperCase()));
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/full-profile")
    public ResponseEntity<Map<String, Object>> getUserFullProfile(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();

        List<Reservation> history = reservationRepository.findByUserId(user.getUsername());
        Map<String, Object> profile = new HashMap<>();
        profile.put("user", user);

        long completed = history.stream()
                .filter(r -> r.getStatus() != null && "ACCEPTED".equals(r.getStatus().name()))
                .count();
        long rejected = history.stream()
                .filter(r -> r.getStatus() != null && "REJECTED".equals(r.getStatus().name()))
                .count();

        profile.put("stats", Map.of(
                "totalSubmitted", history.size(),
                "completed", completed,
                "rejected", rejected
        ));

        profile.put("history", history);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Użytkownik nie ma przypisanego adresu e-mail!"));
        }

        String newTempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(newTempPassword);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), newTempPassword);

        return ResponseEntity.ok(Map.of("message", "Hasło zresetowane i wysłane na e-mail!"));
    }

    // --- NOWY ENDPOINT DO EDYCJI ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserDetails(@PathVariable Long id, @RequestBody UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDepartment(dto.getDepartment());

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Zaktualizowano dane użytkownika"));
    }

    // --- KLASA POMOCNICZA DTO ---
    public static class UpdateUserDto {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String department;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }
}