package pl.gogacz.planner.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.gogacz.planner.core.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserId(String userId, Pageable pageable);

    // --- Wyszukiwanie dla Admina/Pracownika (po sprzęcie, statusie lub pracowniku) ---
    @Query("SELECT r FROM Reservation r WHERE " +
            ":search IS NULL OR LOWER(r.resource.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.assignedEmployee) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Reservation> findAllWithSearch(@Param("search") String search, Pageable pageable);

    // --- Wyszukiwanie dla zwykłego Użytkownika (tylko jego wnioski) ---
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND " +
            "(:search IS NULL OR LOWER(r.resource.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.status) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Reservation> findByUserIdWithSearch(@Param("userId") String userId, @Param("search") String search, Pageable pageable);
}