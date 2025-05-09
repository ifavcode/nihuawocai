package cn.guetzjb.drawguess.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTalkDTO {

    private String id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private Date createTime;
}
