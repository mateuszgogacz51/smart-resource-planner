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
    CommandLineRunner initDatabase(ResourceRepository resourceRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. DODAWANIE ADMINA (jeśli nie istnieje)
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Szyfrujemy!
                admin.setEmail("admin@smart.pl");
                admin.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));
                userRepository.save(admin);
                System.out.println("✅ Dodano użytkownika admin (admin123)");
            }

            // 2. DODAWANIE ZASOBÓW (jeśli nie istnieją)
            if (resourceRepository.count() == 0) {
                Resource res1 = new Resource();
                res1.setName("Laptop Dell XPS 15");
                res1.setDescription("i7, 32GB RAM");
                res1.setStatus(ResourceStatus.AVAILABLE);

                Resource res2 = new Resource();
                res2.setName("Projektor EPSON");
                res2.setDescription("Salka Konferencyjna A");
                res2.setStatus(ResourceStatus.AVAILABLE);

                resourceRepository.save(res1);
                resourceRepository.save(res2);
                System.out.println("✅ Dodano zasoby testowe do bazy!");
            }
        };
    }
}