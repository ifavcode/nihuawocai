package cn.guetzjb.drawguess.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StartGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn()
    @JsonIgnoreProperties({"startGameList", "gameRoundList", "password"})
    private User user;

    private String roomName;

    @Comment("房间总人数")
    private Integer joinedCount;

    private Date creteTime;

    @OneToMany(mappedBy = "startGame")
    @JsonIgnoreProperties({"startGame", "user"})
    private List<GameRound> gameRoundList;

}
