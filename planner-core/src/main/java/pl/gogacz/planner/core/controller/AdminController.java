package pl.gogacz.planner.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    // Pobieranie listy użytkowników wraz z ich statystykami
    @GetMapping("/users/stats")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ADMIN')")
    public List<UserStatsResponse> getUserStats() {
        // 1. Pobieramy wszystkich użytkowników z bazy
        List<User> users = userRepository.findAll();

        // 2. Przekształcamy każdego użytkownika w obiekt statystyk (UserStatsResponse)
        return users.stream().map(user -> {
            // Pobieramy wnioski tylko dla tego konkretnego użytkownika
            List<Reservation> userReservations = reservationRepository.findByUserId(user.getUsername());

            UserStatsResponse stats = new UserStatsResponse();
            stats.setUsername(user.getUsername());
            stats.setEmail(user.getEmail());
            String userRole = (user.getRoles() != null && !user.getRoles().isEmpty())
                    ? user.getRoles().get(0)
                    : "USER";
            stats.setRole(userRole);

            // Obliczamy statystyki
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
}