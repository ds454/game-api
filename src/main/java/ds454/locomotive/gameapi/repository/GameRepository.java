package ds454.locomotive.gameapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ds454.locomotive.gameapi.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByAppId(List<Long> appId);
}
