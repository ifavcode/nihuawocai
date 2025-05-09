package cn.guetzjb.drawguess.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DrawEnum {

    GET_ONLINE_USERS("get_online_users"),
    TALK_EVERYONE("talk_everyone"),
    DRAW("draw"),
    SEAT_DOWN("seat_down"),
    START_GAME("start_game"),
    NEXT_ROUND("next_round"),
    GAME_OVER("game_over"),
    GUESS_CORRECT("guess_correct"),
    REFRESH_ROOM_STATUS("refresh_room_status"),
    ;

    private final String name;
}
