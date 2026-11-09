package ds454.locomotive.gameapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "achievement",
    uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "api_name"})
)

public class Achievement {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    //game_id
    //it has a many to one relationship
    // the lazy makes it so it is only loaded if asked
    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn (name = "game_id")
    private Game game;

    //stream internal id 
    @Column(name = "api_name", nullable = false)
    private String apiName;

    //name of the achievement
    private String title;

    //description
    @Column(columnDefinition = "Text")
    private String description;


    public Achievement(){}

    public Achievement(Game game, String apiName, String title, String description){
        this.title = title;
        this.game = game;
        this.apiName = apiName;
        this.description = description;
    }
    
}
