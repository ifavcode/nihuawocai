package cn.guetzjb.drawguess.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomUserDTO {

    private Integer position;
    private UserDTO user;
    private Integer score;
}
