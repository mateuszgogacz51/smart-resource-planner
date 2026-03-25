package pl.gogacz.planner.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.gogacz.planner.core.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findAll(Pageable pageable);
    Page<Reservation> findByUserId(String userId, Pageable pageable);
}