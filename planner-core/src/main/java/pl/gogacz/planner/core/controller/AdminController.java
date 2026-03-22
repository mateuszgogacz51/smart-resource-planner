package pl.gogacz.planner.core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.dto.UserResponse;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.model.Role;
import pl.gogacz.planner.core.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Usunięto RoleRepository

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        // Role to już List<String>, wystarczy zamienić na Set<String>
                        new HashSet<>(user.getRoles())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/register-employee")
    public ResponseEntity<String> registerEmployee(@RequestBody User employeeRequest) {
        if (userRepository.findByUsername(employeeRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Użytkownik o podanej nazwie już istnieje!");
        }

        // Szyfrujemy hasło przed zapisem
        employeeRequest.setPassword(passwordEncoder.encode(employeeRequest.getPassword()));

        // Przypisujemy rolę pracownika bezpośrednio jako String z Enuma
        employeeRequest.setRoles(List.of(Role.ROLE_EMPLOYEE.name()));

        userRepository.save(employeeRequest);

        return ResponseEntity.ok("Pracownik zarejestrowany pomyślnie!");
    }
}