package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gogacz.planner.core.model.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Ta metoda pozwoli znaleźć rolę po nazwie (np. "ROLE_ADMIN")
    Optional<Role> findByName(String name);
}