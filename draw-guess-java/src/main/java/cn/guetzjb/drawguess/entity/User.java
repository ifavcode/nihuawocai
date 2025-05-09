package cn.guetzjb.drawguess.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String nickname;

    private String password;

    private String avatar;

    private Date createTime;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user", "gameRoundList"})
    private List<StartGame> startGameList;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user", "startGame"})
    private List<GameRound> gameRoundList;
}
