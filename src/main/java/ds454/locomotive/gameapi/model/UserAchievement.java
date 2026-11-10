package ds454.locomotive.gameapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity 
@Table(name = "user_achievements",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
)

public class UserAchievement {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn (name = "achievement_id")
    private Achievement achievement;

    @Setter
    private boolean completed;

    @Setter 
    private Long timeCompleted;
    
    public UserAchievement(){}

    public UserAchievement(UUID userId, Achievement achievement){
        this.userId = userId;
        this.achievement = achievement;
    }

}