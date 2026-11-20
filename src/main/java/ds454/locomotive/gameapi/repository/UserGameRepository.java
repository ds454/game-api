package ds454.locomotive.gameapi.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ds454.locomotive.gameapi.model.Game;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import ds454.locomotive.gameapi.model.UserGame;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    @EntityGraph (attributePaths = "game")
    List<UserGame> findByUserId(UUID userId);

    @Query ("select ug.game from UserGame ug where ug.userId = :userId")
    List<Game> getGamesByUserId(@Param("userId") UUID userId);
    @Query ("select g from Game g where g.id in (" + 
            "select ug.game.id from UserGame ug " + 
            "where ug.userId in :userIds " +
            "group by ug.game.id " +
            "having count(distinct ug.userId) = :userCount)")
    List<Game> findCommonGames( @Param("userIds") Collection<UUID> userIds,
                                @Param("userCount") long userCount);
}
