package cn.guetzjb.drawguess.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn()
    @JsonIgnoreProperties({"gameRoundList", "user"})
    private StartGame startGame;

    private Integer round;

    private Integer seat;

    @ManyToOne
    @JoinColumn()
    private DrawTitle drawTitle;

    private String imageUrl;

    @ManyToOne
    @JoinColumn()
    @JsonIgnoreProperties({"gameRoundList", "startGameList"})
    private User user;

    private Date createTime;

}
