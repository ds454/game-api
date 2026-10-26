package ds454.locomotive.gameapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "games")

public class Game {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(nullable = false, unique = true)
    private long appId;

    private String title;
    
    private String description;

    //under assumption that the user is going to track the progress for said game
    //so I can store as little games as possible
    //private String progress; 

    public Game(){}

    public Game(long appId, String title, String description){
        this.title = title;
        this.appId = appId;
        this.description = description;
    }
}




