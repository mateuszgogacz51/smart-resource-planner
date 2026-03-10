package pl.gogacz.planner.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.dto.UserResponse;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.model.Role;
import pl.gogacz.planner.core.repository.UserRepository;
import pl.gogacz.planner.core.repository.RoleRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Pobieranie listy wszystkich użytkowników (dla tabeli w panelu Admina)
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Rejestracja nowego pracownika (lub admina) przez obecnego Admina
    @PostMapping("/register-employee")
    public ResponseEntity<String> registerEmployee(@RequestBody User employeeRequest) {
        if (userRepository.findByUsername(employeeRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Użytkownik o podanej nazwie już istnieje!");
        }

        // Szyfrujemy hasło przed zapisem
        employeeRequest.setPassword(passwordEncoder.encode(employeeRequest.getPassword()));

        // Domyślnie przypisujemy rolę pracownika (ROLE_WORKER / ROLE_EMPLOYEE)
        // Zakładam, że masz rolę ROLE_EMPLOYEE w bazie
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Błąd: Nie znaleziono roli ROLE_EMPLOYEE w bazie."));

        employeeRequest.setRoles(Set.of(employeeRole));
        userRepository.save(employeeRequest);

        return ResponseEntity.ok("Pracownik zarejestrowany pomyślnie!");
    }
}