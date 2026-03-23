package pl.gogacz.planner.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.dto.UserStatsResponse;
import pl.gogacz.planner.core.model.Reservation;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.repository.ReservationRepository;
import pl.gogacz.planner.core.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/users/stats")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ADMIN')")
    public List<UserStatsResponse> getUserStats() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {
            List<Reservation> userReservations = reservationRepository.findByUserId(user.getUsername());

            UserStatsResponse stats = new UserStatsResponse();
            stats.setUsername(user.getUsername());
            stats.setEmail(user.getEmail());
            String userRole = (user.getRoles() != null && !user.getRoles().isEmpty())
                    ? user.getRoles().get(0)
                    : "USER";
            stats.setRole(userRole);

            stats.setTotalApplications(userReservations.size());
            stats.setAcceptedApplications((int) userReservations.stream()
                    .filter(r -> r.getStatus().name().equals("ACCEPTED")).count());
            stats.setPendingApplications((int) userReservations.stream()
                    .filter(r -> r.getStatus().name().equals("PENDING")).count());
            stats.setRejectedApplications((int) userReservations.stream()
                    .filter(r -> r.getStatus().name().equals("REJECTED")).count());

            return stats;
        }).collect(Collectors.toList());
    }

    // --- NOWE: Zmiana roli użytkownika ---
    @PatchMapping("/users/{username}/role")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<?> changeUserRole(@PathVariable String username, @RequestParam String newRole) {
        // Szukamy użytkownika
        User user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika: " + username));

        // Nadpisujemy starą listę ról nową rolą
        user.setRoles(List.of(newRole));
        userRepository.save(user);

        return ResponseEntity.ok().body("{\"message\": \"Rola zmieniona pomyślnie na " + newRole + "\"}");
    }
}