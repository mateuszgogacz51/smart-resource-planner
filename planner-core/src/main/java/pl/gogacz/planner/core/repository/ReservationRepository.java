package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gogacz.planner.core.model.Reservation;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Metoda pozwalająca pobrać wnioski przypisane do konkretnego loginu użytkownika
    List<Reservation> findByUserId(String userId);
}