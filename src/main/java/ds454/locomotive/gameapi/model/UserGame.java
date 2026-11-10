package ds454.locomotive.gameapi.model;

import java.util.UUID;
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

//goal is to say something along the lines of this user has these games 

@Getter
@Entity 
@Table(name = "user_games",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"})
)

public class UserGame {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn (name = "game_id")
    private Game game;

    public UserGame(){}

    public UserGame(UUID userId, Game game){
        this.userId = userId;
        this.game = game;
    }

}
