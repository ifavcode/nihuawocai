package cn.guetzjb.drawguess.controller;

import cn.guetzjb.drawguess.entity.*;
import cn.guetzjb.drawguess.repository.GameRoundRepository;
import cn.guetzjb.drawguess.repository.StartGameRepository;
import cn.guetzjb.drawguess.repository.UserRepository;
import cn.guetzjb.drawguess.service.DrawService;
import cn.guetzjb.drawguess.service.RedisService;
import cn.guetzjb.drawguess.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/draw")
@RequiredArgsConstructor
public class DrawController {

    private final RedisService redisService;
    private final DrawService drawService;
    private final StartGameRepository startGameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final UserRepository userRepository;

    @GetMapping("/room/status")
    public R roomStatus(@RequestParam String room) {
        RoomStatus roomStatus = drawService.getRoomStatus(room);
        roomStatus.setSeconds(roomStatus.getSeconds() - 10);
        if (roomStatus.getSeconds() <= 0) {
            roomStatus = RoomStatus.builder().room("").round(-1).seconds(-1L).startGameId(-1L).roomUserList(new ArrayList<>()).drawTitle(new DrawTitle(-1L, "")).build();
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

    @GetMapping("/recommend")
    public R recommend() {
        return R.ok(gameRoundRepository.findTop12ValidImage());
    }

    public record GameRoundByStartGame(long startGameId, String imageUrl) {
    }

    @PostMapping("/gameRoundByStartGame")
    public R saveGameRoundByStartGame(@RequestBody GameRoundByStartGame gameRoundByStartGame) {
        Optional<StartGame> byId = startGameRepository.findById(gameRoundByStartGame.startGameId);
        if (byId.isPresent()) {
            StartGame startGame = byId.get();
            List<GameRound> gameRoundList = startGame.getGameRoundList();
            GameRound last = gameRoundList.getLast();
            last.setImageUrl(gameRoundByStartGame.imageUrl);
            startGameRepository.save(startGame);
        } else {
            return R.failed();
        }
        return R.ok();
    }

    @GetMapping("/onlineRoom")
    public R onlineRoom() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, List<RoomUser>> entry : WebSocketService.roomMap.entrySet()) {
            Map<String, Object> result = new HashMap<>();
            result.put("roomName", entry.getKey());
            List<RoomUser> value = entry.getValue();
            List<RoomUser> copy = new ArrayList<>();
            for (RoomUser roomUser : value) {
                copy.add(
                        RoomUser.builder()
                                .position(roomUser.getPosition())
                                .user(roomUser.getUser())
                                .score(roomUser.getScore())
                                .client(null)
                                .build()
                );
            }
            result.put("roomUserList", copy);
            list.add(result);
        }
        return R.ok(list);
    }
}
