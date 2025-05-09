package cn.guetzjb.drawguess.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.drawguess.config.AppConfig;
import cn.guetzjb.drawguess.entity.User;
import cn.guetzjb.drawguess.service.RedisService;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class SecurityUtils {

    private RedisService redisService = new RedisService();

    public User getUser() {
        return (User) StpUtil.getSession().get("user");
    }

    public Long getUserIdWs(SocketIOClient client){
        String token = client.getHandshakeData().getUrlParams().get(AppConfig.TOKEN_NAME).getFirst();
        Object loginIdByToken = StpUtil.getLoginIdByToken(token);
        if (loginIdByToken == null) {
            log.error("loginIdByToken is null");
            return null;
        }
        return Long.parseLong(loginIdByToken.toString());
    }
}
