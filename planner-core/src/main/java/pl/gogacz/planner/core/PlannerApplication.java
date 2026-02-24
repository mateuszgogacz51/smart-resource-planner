package pl.gogacz.planner.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Dodaliśmy scanBasePackages, żeby Spring przeszukał WSZYSTKIE moduły
@SpringBootApplication(scanBasePackages = "pl.gogacz.planner")
public class PlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlannerApplication.class, args);
    }
}