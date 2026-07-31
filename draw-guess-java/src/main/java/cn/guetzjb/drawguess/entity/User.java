package cn.guetzjb.drawguess.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String nickname;

    private String password;

    private String avatar;

    private Date createTime;

    private String openid;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user", "gameRoundList"})
    private List<StartGame> startGameList;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user", "startGame"})
    private List<GameRound> gameRoundList;
}
