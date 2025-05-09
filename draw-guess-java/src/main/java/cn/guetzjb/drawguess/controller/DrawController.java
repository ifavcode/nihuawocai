package cn.guetzjb.drawguess.controller;

import cn.guetzjb.drawguess.entity.DrawTitle;
import cn.guetzjb.drawguess.entity.R;
import cn.guetzjb.drawguess.entity.RoomStatus;
import cn.guetzjb.drawguess.entity.StartGame;
import cn.guetzjb.drawguess.repository.StartGameRepository;
import cn.guetzjb.drawguess.service.DrawService;
import cn.guetzjb.drawguess.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/draw")
@RequiredArgsConstructor
public class DrawController {

    private final RedisService redisService;
    private final DrawService drawService;
    private final StartGameRepository startGameRepository;

    @GetMapping("/room/status")
    public R roomStatus(@RequestParam String room) {
        RoomStatus roomStatus = drawService.getRoomStatus(room);
        roomStatus.setSeconds(roomStatus.getSeconds() - 10);
        if (roomStatus.getSeconds() <= 0) {
            roomStatus = RoomStatus.builder().room("").round(-1).seconds(-1L).startGameId(-1L)
                    .roomUserList(new ArrayList<>()).drawTitle(new DrawTitle(-1L, "")).build();
        }
        return R.ok(roomStatus);
    }

    @GetMapping("/room/lastRecord")
    public R roomRecord(@RequestParam(required = false, defaultValue = "public") String room) {
        StartGame startGame = startGameRepository.findFirstByRoomNameOrderByIdDesc(room);
        if (startGame != null) {
            startGame.getGameRoundList().sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        }
        return R.ok(startGame);
    }
}
