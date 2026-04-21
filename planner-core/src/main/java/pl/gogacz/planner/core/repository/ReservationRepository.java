package pl.gogacz.planner.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gogacz.planner.core.model.Reservation;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Wykorzystywane przez profil audytowy
    List<Reservation> findByUserId(String userId);

    Page<Reservation> findByUserId(String userId, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE " +
            ":search IS NULL OR LOWER(r.resource.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.assignedEmployee) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Reservation> findAllWithSearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND " +
            "(:search IS NULL OR LOWER(r.resource.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Reservation> findByUserIdWithSearch(@Param("userId") String userId, @Param("search") String search, Pageable pageable);

    // === NOWE: Zapytanie do statystyk (Ranking pracowników) ===
    @Query("SELECT r.assignedEmployee, COUNT(r) FROM Reservation r WHERE r.assignedEmployee IS NOT NULL GROUP BY r.assignedEmployee ORDER BY COUNT(r) DESC")
    List<Object[]> getEmployeeRanking();
}