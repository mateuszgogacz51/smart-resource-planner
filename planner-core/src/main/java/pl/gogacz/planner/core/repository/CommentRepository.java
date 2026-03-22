package pl.gogacz.planner.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gogacz.planner.core.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}