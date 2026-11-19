package ds454.locomotive.gameapi.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ds454.locomotive.gameapi.model.Game;

import java.util.List;
import java.util.UUID;

import ds454.locomotive.gameapi.model.UserGame;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    @EntityGraph (attributePaths = "game")
    List<UserGame> findByUserId(UUID userId);

    @Query ("select ug.game from UserGame ug where ug.userId = :userId")
    List<Game> getGamesByUserId(@Param("userId") UUID userId);
}
