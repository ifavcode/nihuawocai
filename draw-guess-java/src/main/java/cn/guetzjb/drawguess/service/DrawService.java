package cn.guetzjb.drawguess.service;

import cn.guetzjb.drawguess.constant.RedisConstant;
import cn.guetzjb.drawguess.entity.DrawTitle;
import cn.guetzjb.drawguess.entity.R;
import cn.guetzjb.drawguess.entity.RoomStatus;
import cn.guetzjb.drawguess.exception.ServiceException;
import cn.guetzjb.drawguess.repository.DrawTitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawService {

    private final RedisService redisService;
    private final DrawTitleRepository drawTitleRepository;

    public RoomStatus getRoomStatus(String room) {
        String key = String.format(RedisConstant.GAME_ROUND, room);
        Object cacheObject = redisService.getCacheObject(key);
        if (cacheObject == null) {
            return RoomStatus.builder().room("").round(-1).seconds(-1L).startGameId(-1L).roomUserList(new ArrayList<>()).build();
        }
        RoomStatus roomStatus = cacheObject instanceof RoomStatus ? (RoomStatus) cacheObject : null;
        if (roomStatus != null) {
            long expire = redisService.getExpire(key);
            roomStatus.setSeconds(expire);
        }
        return roomStatus;
    }

    public DrawTitle generateRandomDrawTitle() {
        long count = drawTitleRepository.count();
        if (count == 0) {
            log.info("画画题目为空，请导入题目集!");
            return new DrawTitle(-1L, "测试");
        }
        Random random = new Random();
        long offset = random.nextLong(count);
        PageRequest request = PageRequest.of((int) offset, 1);
        return drawTitleRepository.findAll(request).getContent().getFirst();
    }

}
