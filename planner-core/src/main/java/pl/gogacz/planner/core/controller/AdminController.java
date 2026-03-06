package pl.gogacz.planner.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.repository.UserRepository;
import pl.gogacz.planner.core.security.CreateUserRequest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Użytkownik już istnieje!");
        }

        // Używamy klasycznego tworzenia obiektu zamiast wzorca Builder
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password())); // Od razu szyfrujemy!
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(request.roles());

        userRepository.save(user);
        return ResponseEntity.ok("Sukces! Użytkownik " + user.getUsername() + " dodany do systemu.");
    }
}