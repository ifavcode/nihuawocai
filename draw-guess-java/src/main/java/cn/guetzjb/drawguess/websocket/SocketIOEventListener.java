package cn.guetzjb.drawguess.websocket;

import cn.guetzjb.drawguess.config.AppConfig;
import cn.guetzjb.drawguess.entity.DrawEvent;
import cn.guetzjb.drawguess.entity.dto.GameRoundDTO;
import cn.guetzjb.drawguess.entity.dto.UserTalkDTO;
import cn.guetzjb.drawguess.enums.DrawEnum;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketIOEventListener {

    private final SocketIOServer socketIOServer;
    private final WebSocketService webSocketService;

    @Autowired
    public SocketIOEventListener(SocketIOServer socketIOServer, WebSocketService webSocketService) {
        this.socketIOServer = socketIOServer;
        this.webSocketService = webSocketService;
    }

    @PostConstruct
    private void init() {
        SocketIONamespace ns = socketIOServer.addNamespace(AppConfig.NAMESPACE);

        ns.addConnectListener(client -> {
            log.info("Client connected event fired: {}", client.getSessionId());
            webSocketService.connect(client);
        });

        ns.addDisconnectListener(client -> {
            webSocketService.disconnect(client);
        });

        ns.addEventListener(DrawEnum.GET_ONLINE_USERS.getName(), DrawEvent.class, (client, data, ackRequest) -> {
            webSocketService.getOnlineUsers(client, false);
        });

        ns.addEventListener(DrawEnum.TALK_EVERYONE.getName(), UserTalkDTO.class, (client, data, ackRequest) -> {
            webSocketService.talkEveryOne(client, data);
        });

        ns.addEventListener(DrawEnum.DRAW.getName(), DrawEvent.class, (client, data, ackRequest) -> {
            webSocketService.draw(client, data);
        });

        ns.addEventListener(DrawEnum.SEAT_DOWN.getName(), DrawEvent.class, (client, data, ackRequest) -> {
            webSocketService.seatDown(client, data);
        });

        ns.addEventListener(DrawEnum.STAND_UP.getName(), DrawEvent.class, (client, data, ackRequest) -> {
            webSocketService.standUp(client, data);
        });

        ns.addEventListener(DrawEnum.START_GAME.getName(), DrawEvent.class, (client, data, ackRequest) -> {
            webSocketService.startGame(client, data);
        });

        ns.addEventListener(DrawEnum.NEXT_ROUND.getName(), GameRoundDTO.class, (client, data, ackRequest) -> {
            webSocketService.nextRound(client, data);
        });

        socketIOServer.start();
    }

    @PreDestroy
    private void destroy() {
        // 关闭服务器
        socketIOServer.stop();
    }
}
