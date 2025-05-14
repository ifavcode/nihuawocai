package cn.guetzjb.drawguess.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.drawguess.config.AppConfig;
import cn.guetzjb.drawguess.constant.RedisConstant;
import cn.guetzjb.drawguess.entity.*;
import cn.guetzjb.drawguess.entity.dto.*;
import cn.guetzjb.drawguess.enums.DrawEnum;
import cn.guetzjb.drawguess.repository.GameRoundRepository;
import cn.guetzjb.drawguess.repository.StartGameRepository;
import cn.guetzjb.drawguess.service.DrawService;
import cn.guetzjb.drawguess.service.RedisService;
import cn.guetzjb.drawguess.service.UserService;
import cn.guetzjb.drawguess.utils.SecurityUtils;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

    @Value("${draw.max-person}")
    private int maxPerson; // 房间一次最大人数
    @Value("${sa-token.token-name}")
    private String tokenName;
    @Value("${draw.seconds}")
    private Integer drawSeconds;

    private final static Map<SocketIOClient, UserDTO> userMap = new ConcurrentHashMap<>();
    private final static Map<String, List<RoomUser>> roomMap = new ConcurrentHashMap<>();
    private final UserService userService;
    private final RedisService redisService;
    private final StartGameRepository startGameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final DrawService drawService;

    public void connect(SocketIOClient client) {
        String room = getRoom(client);
        String token = getToken(client);
        Object loginIdByToken = StpUtil.getLoginIdByToken(token);
        if (loginIdByToken == null) {
            log.error("loginIdByToken is null");
            return;
        }
        long userId = Long.parseLong(loginIdByToken.toString());
        if (roomHasUser(room, userId)) {
            log.info("用户ID:{}，不可重复加入房间:{}", userId, room);
            return;
        }
        User profile = userService.getProfile(userId, true);
        UserDTO user = new UserDTO(userId, profile.getNickname(), profile.getAvatar());
        userMap.put(client, user);
        roomMap.computeIfAbsent(room, k -> new ArrayList<>()).add(new RoomUser(client, null, user, 0));
        client.joinRoom(room);
        RoomStatus roomStatus = drawService.getRoomStatus(room);
        if (roomStatus != null && roomStatus.getStartGameId() != -1L) {
            // 游戏正在进行
            for (RoomUserDTO roomUserDTO : roomStatus.getRoomUserList()) {
                if (roomUserDTO.getUser().equals(user)) {
                    DrawEvent<Integer> event = new DrawEvent<>();
                    event.setName(DrawEnum.SEAT_DOWN.getName());
                    event.setData(roomUserDTO.getPosition());
                    seatDown(client, event); // 已经通知所有用户
                    break;
                }
            }
        } else {
            // 通知所有成员上线
            getOnlineUsers(client, true);
        }
        log.info("{}连接成功，当前在线人数{}", client.getSessionId(), userMap.size());
    }

    public void disconnect(SocketIOClient client) {
        String room = getRoom(client);
        userMap.remove(client);
        String token = getToken(client);
        Object loginIdByToken = StpUtil.getLoginIdByToken(token);
        if (loginIdByToken == null) {
            log.error("disconnect loginIdByToken is null");
            return;
        }
        long userId = Long.parseLong(loginIdByToken.toString());
        if (roomMap.containsKey(room)) {
            List<RoomUser> users = roomMap.get(room);
            if (users != null) {
                users.removeIf(roomUser -> roomUser.getUser().getId().equals(userId));
            }
        }
        client.leaveRoom(room);
        getOnlineUsers(client, true);
        log.info("{}断开成功，当前在线人数{}", client.getSessionId(), userMap.size());
    }

    public void getOnlineUsers(SocketIOClient client, boolean all) {
        List<RoomUserDTO> in = new ArrayList<>(maxPerson); //房间内——已入座
        List<RoomUserDTO> out = new ArrayList<>(); //房间内
        for (int i = 0; i < maxPerson; i++) {
            in.add(new RoomUserDTO(i + 1, new UserDTO(-1L, "加入", ""), 0));
        }
        String room = getRoom(client);
        for (SocketIOClient socketIOClient : client.getNamespace().getRoomOperations(room).getClients()) {
            RoomUser roomUser = getRoomUser(room, socketIOClient);
            if (roomUser != null) {
                if (roomUser.getPosition() != null) {
                    in.set(roomUser.getPosition(), new RoomUserDTO(roomUser.getPosition(), roomUser.getUser(), roomUser.getScore()));
                } else {
                    out.add(new RoomUserDTO(null, roomUser.getUser(), roomUser.getScore()));
                }
            }
        }
        Map<String, List<RoomUserDTO>> map = new HashMap<>();
        map.put("in", in);
        map.put("out", out);
        if (all) {
            client.getNamespace().getRoomOperations(room).sendEvent(DrawEnum.GET_ONLINE_USERS.getName(), map);
        } else {
            client.sendEvent(DrawEnum.GET_ONLINE_USERS.getName(), map);
        }
    }

    // 回复房间内所有人
    @Transactional
    public void talkEveryOne(SocketIOClient client, UserTalkDTO userTalkDTO) {
        String room = getRoom(client);
        RoomStatus roomStatus = drawService.getRoomStatus(room);
        boolean send = true;
        if (roomStatus != null && roomStatus.getStartGameId() != -1L) {
            String ans = roomStatus.getDrawTitle().getTitle();
            if (ans.equals(userTalkDTO.getContent().trim())) {
                // 猜对了
                userTalkDTO.setContent(userTalkDTO.getContent().replace(ans, "*".repeat(ans.length())));
                String key = String.format(RedisConstant.GAME_ROUND_GUESS_CORRECT, room, roomStatus.getRound());
                Set<Long> correctUserIdSet = new HashSet<>();
                if (!redisService.hasKey(key)) {
                    redisService.setCacheSet(key, correctUserIdSet, drawSeconds + 10, TimeUnit.SECONDS);
                }
                correctUserIdSet = redisService.getCacheSet(key);
                if (!correctUserIdSet.contains(userTalkDTO.getUserId())) {
                    correctUserIdSet.add(userTalkDTO.getUserId());
                    send = false;
                    UserDTO correctUser = null;
                    for (int i = 0; i < roomStatus.getRoomUserList().size(); i++) {
                        RoomUserDTO drawUser = roomStatus.getRoomUserList().get(i);
                        if (i == roomStatus.getRound()) {
                            drawUser.setScore(drawUser.getScore() + 1);// 画画者 +1 分
                            // 同步本地roomMap
                            for (RoomUser roomUser : roomMap.get(room)) {
                                if (roomUser.getUser().equals(drawUser.getUser())) {
                                    roomUser.setScore(roomUser.getScore() + 1);
                                    break;
                                }
                            }
                        }
                        if (drawUser.getUser().getId().equals(userTalkDTO.getUserId())) {
                            drawUser.setScore(drawUser.getScore() + 1);// 猜对者 +1 分
                            correctUser = drawUser.getUser();
                            // 同步本地roomMap
                            for (RoomUser roomUser : roomMap.get(room)) {
                                if (roomUser.getUser().equals(drawUser.getUser())) {
                                    roomUser.setScore(roomUser.getScore() + 1);
                                    break;
                                }
                            }
                        }
                    }
                    redisService.setCacheSet(key, correctUserIdSet, drawSeconds + 10, TimeUnit.SECONDS);

                    // 更新key
                    String gameRoundKey = String.format(RedisConstant.GAME_ROUND, room);
                    long expire = redisService.getExpire(gameRoundKey);
                    redisService.setCacheObject(gameRoundKey, roomStatus, expire, TimeUnit.SECONDS);
                    expire = Math.max(0, expire - 10);// 真实过期时间-10s
                    if (correctUserIdSet.size() >= roomStatus.getRoomUserList().size() - 1) {
                        // 除去画画者，全部人都猜对
                        expire = 5;// 5s后下一轮
                    }
                    roomStatus.setSeconds(expire);
                    // 用户猜对，发送房间最新状态
                    client.getNamespace().getRoomOperations(getRoom(client)).sendEvent(
                            DrawEnum.REFRESH_ROOM_STATUS.getName(), roomStatus
                    );
                    client.getNamespace().getRoomOperations(getRoom(client)).sendEvent(
                            DrawEnum.GUESS_CORRECT.getName(), correctUser
                    );
                }
            }
        }
        if (send) {
            client.getNamespace().getRoomOperations(getRoom(client)).sendEvent(
                    DrawEnum.TALK_EVERYONE.getName(), userTalkDTO
            );
        }
    }

    private String getRoom(SocketIOClient client) {
        // 公共房间为public
        Map<String, List<String>> urlParams = client.getHandshakeData().getUrlParams();
        return urlParams.getOrDefault("room", Collections.singletonList("public")).getFirst();
    }

    private String getToken(SocketIOClient client) {
        // 公共房间为public
        Map<String, List<String>> urlParams = client.getHandshakeData().getUrlParams();
        return urlParams.getOrDefault(AppConfig.TOKEN_NAME, Collections.singletonList("")).getFirst();
    }

    private boolean isSeat(SocketIOClient client, Integer seat) {
        // 获取房间某个位置是否被占用
        String room = getRoom(client);
        List<RoomUser> roomUsers = roomMap.getOrDefault(room, new ArrayList<>());
        if (roomUsers.isEmpty()) {
            return false;
        }
        boolean flag = false;
        for (RoomUser roomUser : roomUsers) {
            if (roomUser.getPosition() != null && roomUser.getPosition().equals(seat)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private RoomUser getRoomUser(String room, SocketIOClient client) {
        List<RoomUser> list = roomMap.getOrDefault(room, new ArrayList<>());
        for (RoomUser roomUser : list) {
            if (roomUser.getClient().equals(client)) {
                return roomUser;
            }
        }
        return null;
    }

    private List<RoomUser> getRoomSeatDownUser(String room) {
        List<RoomUser> list = roomMap.getOrDefault(room, new ArrayList<>());
        List<RoomUser> back = new ArrayList<>();
        for (RoomUser roomUser : list) {
            if (roomUser.getPosition() != null) {
                back.add(roomUser);
            }
        }
        return back;
    }

    private boolean roomHasUser(String room, long userId) {
        List<RoomUser> list = roomMap.getOrDefault(room, new ArrayList<>());
        for (RoomUser roomUser : list) {
            if (roomUser.getUser().getId() == userId) {
                return true;
            }
        }
        return false;
    }

    public void draw(SocketIOClient client, DrawEvent<List<DrawHistory>> drawEvent) {
        for (SocketIOClient socketIOClient : client.getNamespace().getRoomOperations(getRoom(client)).getClients()) {
            if (socketIOClient != client) {
                socketIOClient.sendEvent(DrawEnum.DRAW.getName(), drawEvent);
            }
        }
    }

    public void seatDown(SocketIOClient client, DrawEvent<Integer> drawEvent) {
        String room = getRoom(client);
        Integer seat = drawEvent.getData();
        if (isSeat(client, seat)) {
            log.info("座位{}已被占用", seat);
            return;
        }
        List<RoomUser> list = roomMap.getOrDefault(room, new ArrayList<>());
        for (RoomUser roomUser : list) {
            if (roomUser.getClient().equals(client)) {
                roomUser.setPosition(seat);
            }
        }
        // 通知房间内所有用户，座位改变
        getOnlineUsers(client, true);
    }

    public void standUp(SocketIOClient client, DrawEvent<Void> drawEvent) {
        String room = getRoom(client);
        List<RoomUser> list = roomMap.getOrDefault(room, new ArrayList<>());
        for (RoomUser roomUser : list) {
            if (roomUser.getClient().equals(client)) {
                roomUser.setPosition(null);
            }
        }
        getOnlineUsers(client, true);
    }

    @Transactional
    public void startGame(SocketIOClient client, DrawEvent<StartGameDTO> data) {
        String room = getRoom(client);
        StartGame startGame = new StartGame();
        int size = roomMap.getOrDefault(room, new ArrayList<>()).size();
        startGame.setJoinedCount(size);
        Long userId = SecurityUtils.getUserIdWs(client);
        User user = userService.getProfile(userId, true);
        startGame.setUser(user);
        startGame.setGameRoundList(new ArrayList<>());
        startGame.setRoomName(room);
        startGame.setCreteTime(new Date());
        startGameRepository.save(startGame);
        // 存储房间内用户状态
        List<RoomUser> roomUsers = getRoomSeatDownUser(room);
        DrawTitle drawTitle = drawService.generateRandomDrawTitle();
        List<RoomUserDTO> roomUserDTOs = new ArrayList<>(roomUsers.stream().map(v -> new RoomUserDTO(v.getPosition(), v.getUser(), 0)).toList());
        roomUserDTOs.sort(Comparator.comparing(RoomUserDTO::getPosition));
        RoomStatus roomStatus = RoomStatus.builder().room(room).seconds((long) drawSeconds).round(0)
                .startGameId(startGame.getId()).roomUserList(roomUserDTOs).drawTitle(drawTitle).build();
        redisService.setCacheObject(String.format(RedisConstant.GAME_ROUND, room), roomStatus, (long) drawSeconds + 10, TimeUnit.SECONDS); // 多十秒缓冲
        // 生成唯一游戏ID，并告知房间内用户
        client.getNamespace().getRoomOperations(room).sendEvent(DrawEnum.START_GAME.getName(), roomStatus);
    }

    public void nextRound(SocketIOClient client, GameRoundDTO dto) {
        // 谁画完了谁来调用下一轮
        String room = getRoom(client);
        Object cacheObject = redisService.getCacheObject(String.format(RedisConstant.GAME_ROUND, room));
        if (cacheObject == null) {
            client.getNamespace().getRoomOperations(room).sendEvent(DrawEnum.GAME_OVER.getName());
            return;
        }
        // 保存当前轮数据
        GameRound gameRound = new GameRound();
        gameRound.setRound(dto.getRound());
        gameRound.setSeat(dto.getSeat());
        gameRound.setStartGame(startGameRepository.findById(dto.getStartGameId()).orElse(null));
        Long userId = SecurityUtils.getUserIdWs(client);
        User user = userService.getProfile(userId, true);
        gameRound.setUser(user);
        gameRound.setImageUrl(dto.getImageUrl());
        gameRound.setCreateTime(new Date());
        RoomStatus roomStatus = drawService.getRoomStatus(room);
        boolean isTest = roomStatus.getDrawTitle().getId() == -1;
        gameRound.setDrawTitle(roomStatus.getDrawTitle());
        if (roomStatus.getRound() + 1 >= roomStatus.getRoomUserList().size()) {
            if (!isTest) {
                gameRoundRepository.save(gameRound);
            }
            redisService.deleteObject(String.format(RedisConstant.GAME_ROUND, room));
            client.getNamespace().getRoomOperations(room).sendEvent(DrawEnum.GAME_OVER.getName());
        } else {
            gameRound.setDrawTitle(roomStatus.getDrawTitle());
            if (!isTest) {
                gameRoundRepository.save(gameRound);
            }
            roomStatus.setRound(roomStatus.getRound() + 1);
            roomStatus.setSeconds(Long.valueOf(drawSeconds));
            roomStatus.setDrawTitle(drawService.generateRandomDrawTitle());
            redisService.setCacheObject(String.format(RedisConstant.GAME_ROUND, room), roomStatus, (long) drawSeconds + 10, TimeUnit.SECONDS);
            client.getNamespace().getRoomOperations(room).sendEvent(DrawEnum.NEXT_ROUND.getName(), roomStatus);
        }
    }
}
