package ds454.locomotive.gameapi.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import ds454.locomotive.gameapi.model.Achievement;

@Repository 
public interface AchievementRepository extends JpaRepository<Achievement, Long>{
    List<Achievement> findByGameId(long gameId);  
} 

