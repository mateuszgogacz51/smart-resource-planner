package pl.gogacz.planner.core.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.gogacz.planner.core.model.Resource;
import pl.gogacz.planner.core.model.ResourceStatus;
import pl.gogacz.planner.core.model.User;
import pl.gogacz.planner.core.repository.ResourceRepository;
import pl.gogacz.planner.core.repository.UserRepository;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      ResourceRepository resourceRepository,
                                      PasswordEncoder passwordEncoder) { // <-- Wstrzyknięty PasswordEncoder
        return args -> {
            // === 1. ZASILANIE UŻYTKOWNIKÓW (Struktura Firmy) ===
            if (userRepository.count() == 0) {
                System.out.println("🌱 Inicjalizacja bazy użytkowników...");

                // Admini i Obsługa
                userRepository.save(createUser("admin", "admin@firma.pl", "IT", "ROLE_ADMIN", passwordEncoder));
                userRepository.save(createUser("piotr_magazyn", "piotr@firma.pl", "MAGAZYN", "ROLE_EMPLOYEE", passwordEncoder));
                userRepository.save(createUser("anna_it", "anna@firma.pl", "IT", "ROLE_EMPLOYEE", passwordEncoder));

                // Zwykli pracownicy (różne działy)
                userRepository.save(createUser("jan_kowalski", "jan@firma.pl", "HR", "ROLE_USER", passwordEncoder));
                userRepository.save(createUser("kasia_nowak", "kasia@firma.pl", "HR", "ROLE_USER", passwordEncoder));
                userRepository.save(createUser("michal_dyrektor", "michal@firma.pl", "ZARZAD", "ROLE_USER", passwordEncoder));
                userRepository.save(createUser("tomasz_sprzedaz", "tomasz@firma.pl", "SPRZEDAZ", "ROLE_USER", passwordEncoder));
                userRepository.save(createUser("ewelina_ksiegowosc", "ewelina@firma.pl", "KSIEGOWOSC", "ROLE_USER", passwordEncoder));
            }

            // === 2. ZASILANIE MAGAZYNU (Kategorie zasobów) ===
            if (resourceRepository.count() == 0) {
                System.out.println("📦 Inicjalizacja bazy zasobów i magazynu...");

                // Kategoria: LAPTOP / IT
                resourceRepository.save(createResource("Dell Latitude 7420 (S/N: 001)", "LAPTOP"));
                resourceRepository.save(createResource("Dell Latitude 7420 (S/N: 002)", "LAPTOP"));
                resourceRepository.save(createResource("MacBook Pro M2 (S/N: MAC-01)", "LAPTOP"));
                resourceRepository.save(createResource("Projektor Epson 4K", "LAPTOP"));

                // Kategoria: SALE (ROOM)
                resourceRepository.save(createResource("Sala Konferencyjna A (10 os.)", "ROOM"));
                resourceRepository.save(createResource("Sala Konferencyjna B (6 os.)", "ROOM"));
                resourceRepository.save(createResource("Sala Zarządu (20 os.)", "ROOM"));
                resourceRepository.save(createResource("Budka Akustyczna 1", "ROOM"));
                resourceRepository.save(createResource("Budka Akustyczna 2", "ROOM"));

                // Kategoria: POJAZDY (CAR)
                resourceRepository.save(createResource("Toyota Corolla (WA 12345)", "CAR"));
                resourceRepository.save(createResource("Skoda Octavia (LU 98765)", "CAR"));
                resourceRepository.save(createResource("Ford Transit - Dostawczy", "CAR"));

                // Kategoria: INNE (OTHER)
                resourceRepository.save(createResource("Zestaw VR Oculus Quest 3", "OTHER"));
                resourceRepository.save(createResource("Aparat Sony A7 III", "OTHER"));
            }

            System.out.println("✅ Baza danych gotowa do pracy!");
        };
    }

    private User createUser(String username, String email, String department, String role, PasswordEncoder passwordEncoder) { // <-- Przekazany PasswordEncoder
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        // Zabezpieczenie: Hasło "admin123" zostanie zamienione na hash BCrypt!
        user.setPassword(passwordEncoder.encode("admin123"));

        user.setDepartment(department);
        user.setRoles(List.of(role));

        // Jeśli masz pola firstName i lastName w modelu User, możesz tu je łatwo podzielić ze stringa,
        // ale na ten moment ustawiam domyślne.
        user.setFirstName(username.split("_")[0]);
        user.setLastName("Testowy");
        return user;
    }

    private Resource createResource(String name, String type) {
        Resource res = new Resource();
        res.setName(name);
        res.setType(type);
        res.setStatus(ResourceStatus.AVAILABLE);
        return res;
    }
}