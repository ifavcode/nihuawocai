package cn.guetzjb.drawguess.entity.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class GameRoundDTO {

    private Integer round;
    private Integer seat;
    private Long startGameId;
    private String imageUrl;

}
