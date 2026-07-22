package cn.guetzjb.drawguess.entity;

import cn.guetzjb.drawguess.entity.dto.UserDTO;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomUser {

    private SocketIOClient client;
    private Integer position;
    private UserDTO user;
    private Integer score;

    public RoomUser(SocketIOClient client, Integer position) {
        this.client = client;
        this.position = position;
    }
}
