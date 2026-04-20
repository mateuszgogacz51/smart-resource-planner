package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gogacz.planner.core.model.Comment;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByReservationId(Long reservationId);

}