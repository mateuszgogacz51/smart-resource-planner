package pl.gogacz.planner.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Aktywuje automatyczne wypełnianie dat createdAt i updatedAt
public class PlannerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlannerApplication.class, args);
    }
}