package cn.guetzjb.drawguess.entity;


import cn.guetzjb.drawguess.entity.dto.RoomUserDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomStatus {

    private Long startGameId;
    private Integer round;
    private String room;
    private Long seconds;
    private DrawTitle drawTitle;
    private List<RoomUserDTO> roomUserList;
}
