package pl.gogacz.planner.core.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.gogacz.planner.core.model.Resource;
import pl.gogacz.planner.core.model.ResourceStatus;
import pl.gogacz.planner.core.repository.ResourceRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ResourceRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Resource res1 = new Resource();
                res1.setName("Laptop Dell XPS 15");
                res1.setDescription("i7, 32GB RAM");
                res1.setStatus(ResourceStatus.AVAILABLE);

                Resource res2 = new Resource();
                res2.setName("Projektor EPSON");
                res2.setDescription("Salka Konferencyjna A");
                res2.setStatus(ResourceStatus.AVAILABLE);

                repository.save(res1);
                repository.save(res2);
                System.out.println("✅ Dodano zasoby testowe do bazy!");
            }
        };
    }
}