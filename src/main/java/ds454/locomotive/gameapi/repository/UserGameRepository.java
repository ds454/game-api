package ds454.locomotive.gameapi.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import ds454.locomotive.gameapi.model.UserGame;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    @EntityGraph (attributePaths = "game")
    List<UserGame> findByUserId(UUID userId);
}
