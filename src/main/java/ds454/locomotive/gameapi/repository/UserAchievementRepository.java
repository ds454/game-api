package ds454.locomotive.gameapi.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


import ds454.locomotive.gameapi.model.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    @EntityGraph (attributePaths = {"achievement"})
    List<UserAchievement> findByUserIdAndAchievementGameId(UUID userId, long gameId);
   
    @Transactional 
    @Modifying 
    @Query("delete from UserAchievement ua where ua.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);

}
