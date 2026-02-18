package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gogacz.planner.core.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}