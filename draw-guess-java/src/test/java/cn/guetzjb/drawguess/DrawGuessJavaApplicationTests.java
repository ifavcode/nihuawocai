package cn.guetzjb.drawguess;

import cn.guetzjb.drawguess.entity.DrawEvent;
import cn.guetzjb.drawguess.entity.dto.GameRoundDTO;
import cn.guetzjb.drawguess.entity.dto.UserDTO;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DrawGuessJavaApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        UserDTO d1 = new UserDTO();
        UserDTO d2 = new UserDTO();
        d1.setId(1L);
        d2.setId(1L);
        System.out.println(d1.equals(d2));
    }

}
