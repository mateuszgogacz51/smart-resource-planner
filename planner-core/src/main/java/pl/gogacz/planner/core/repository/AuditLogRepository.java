package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gogacz.planner.core.model.AuditLog;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByReservationIdOrderByTimestampDesc(Long reservationId);
}