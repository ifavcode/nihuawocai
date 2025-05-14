package cn.guetzjb.drawguess;

import cn.guetzjb.drawguess.entity.DrawEvent;
import cn.guetzjb.drawguess.entity.dto.GameRoundDTO;
import cn.guetzjb.drawguess.entity.dto.UserDTO;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.aliyun.oss.common.utils.AuthUtils;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DrawGuessJavaApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        System.out.println(System.getenv(AuthUtils.ACCESS_KEY_ENV_VAR));
    }

}
