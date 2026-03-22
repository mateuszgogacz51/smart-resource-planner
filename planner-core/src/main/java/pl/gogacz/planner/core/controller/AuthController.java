package pl.gogacz.planner.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.core.model.Role;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.repository.UserRepository;
import pl.gogacz.planner.core.security.AuthenticationRequest;
import pl.gogacz.planner.core.security.AuthenticationResponse;
import pl.gogacz.planner.core.security.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    // Dodane serwisy do rejestracji
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Zaktualizowany konstruktor
    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        // Twoje logi diagnostyczne
        System.out.println("Próba logowania dla: " + request.username());
        System.out.println("Hasło z żądania: " + request.password());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Użytkownik o takiej nazwie już istnieje!");
        }

        // Szyfrujemy hasło
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // Przypisujemy zwykłą rolę
        userRequest.setRoles(List.of(Role.ROLE_USER.name()));

        userRepository.save(userRequest);

        return ResponseEntity.ok("Rejestracja zakończona sukcesem!");
    }
}