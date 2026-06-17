package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gogacz.planner.core.model.Resource;
import pl.gogacz.planner.core.model.ResourceStatus;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByStatus(ResourceStatus status);
}