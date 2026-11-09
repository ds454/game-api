package ds454.locomotive.gameapi.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;


import ds454.locomotive.gameapi.model.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    @EntityGraph (attributePaths = "achievement")
    List<UserAchievement> findByUserIdAndAchievementGameId(UUID userId, long gameId);
}
