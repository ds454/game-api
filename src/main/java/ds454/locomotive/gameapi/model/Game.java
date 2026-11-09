package ds454.locomotive.gameapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "games")

public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    //stream id for the game, unique so no duplicates
    @Column(nullable = false, unique = true)
    private long appId;

    //will hold GameFromStream.name
    private String title;

    private int releaseYear;
    
    @Column(columnDefinition = "Text")
    private String description;

    public Game(){}

    public Game(long appId, String title, String description, int releaseYear){
        this.title = title;
        this.appId = appId;
        this.description = description;
        this.releaseYear = releaseYear;
    }
}




