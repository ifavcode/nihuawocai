import {
  Constant,
  DrawEnum,
  type DrawEvent,
  type DrawHistory,
  type RoomStatus,
  type RoomUserInOut,
  type UserDTO,
  type UserTalkDTO,
} from "@/types";
import { emitter, inRoom } from "@/utils";
import { defineStore } from "pinia";
import { io, type Socket } from "socket.io-client";
import Cookies from "js-cookie";
import { getRoomStatusApi } from "@/api/draw";
import { useUserStore } from "./userStore";
import router from "@/router";

const host = window.location.host;

export const useGlobalStore = defineStore("global", () => {
  const socket = ref<Socket | null>(null);
  const onlineUsers = ref<RoomUserInOut>({
    in: [],
    out: [],
  });
  const messageList = ref<UserTalkDTO[]>([]);
  const drawHistory = ref<DrawHistory[]>([]);
  const isMobile = ref(
    /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
      navigator.userAgent
    )
  );
  const curSeat = ref<number | null>(null);
  const roomStatus = ref<RoomStatus>({
    startGameId: -1,
    round: -1,
    room: "",
    seconds: -1,
    roomUserList: [],
    drawTitle: {
      id: -1,
      title: "",
    },
  });

  async function init(roomName: string = "public") {
    /**
     * 开发模式时
     * VITE_WS_URL填写http://127.0.0.1:9000/draw-socket 直连不通过nginx
     * VITE_WS_PATH填写/socket.io 不通过nginx
     * 
     * 生产模式时
     * VITE_WS_URL填写/draw-socket自动填写host地址 通过nginx转发
     * VITE_WS_PATH填写/draw-socket/socket.io 通过nginx转发
     */
    socket.value = io(
      (import.meta.env.MODE === "development" ? "" : host) + import.meta.env.VITE_WS_URL +
      `?room=${roomName}&${Constant.JWT_HEADER_NAME}=${Cookies.get(Constant.JWT_HEADER_NAME) || ""
      }`,
      {
        path: import.meta.env.VITE_WS_PATH,
        withCredentials: true,
        secure: true,
        transports: ["websocket"],
      }
    );

    socket.value.on("connect", () => {
      console.log("已连接到服务器");
      // console.log("尝试发送testEvent");
      emitter.emit("testEvent");
      setTimeout(() => {
        emitter.emit("testEvent");
      }, 900);
    });

    socket.value.on(DrawEnum.GET_ONLINE_USERS, (data) => {
      if (roomStatus.value.startGameId === -1) {
        onlineUsers.value = data as RoomUserInOut;
        onlineUsers.value.in.map(v => {
          v.online = true
          return v
        })
        onlineUsers.value.out.map(v => {
          v.online = true
          return v
        })
      } else {
        if (onlineUsers.value.in.length === 0) {
          const onlineUsersInStr = localStorage.getItem('onlineUsersIn')
          if (onlineUsersInStr) {
            onlineUsers.value.in = JSON.parse(onlineUsersInStr)
            const find = onlineUsers.value.in.find(v => v.user.id === useUserStore().user.id)
            if (find) {
              curSeat.value = find.position
            }
          }
        }
        if (onlineUsers.value.out.length === 0) {
          const onlineUsersOutStr = localStorage.getItem('onlineUsersOut')
          if (onlineUsersOutStr) {
            onlineUsers.value.out = JSON.parse(onlineUsersOutStr)
          }
        }
        const onlineIn = (data as RoomUserInOut).in.map(v => {
          return v.user.id
        })
        const onlineOut = (data as RoomUserInOut).out.map(v => {
          return v.user.id
        })
        onlineUsers.value.in.map(v => {
          v.online = onlineIn.includes(v.user.id)
          return v
        })
        onlineUsers.value.out.map(v => {
          v.online = onlineOut.includes(v.user.id)
          return v
        })
      }
      if (!inRoom()) {
        window.$message.warning('已退出房间')
        router.replace({
          name: 'hall'
        })
      }
    });

    socket.value.on(DrawEnum.TALK_EVERYONE, (data) => {
      messageList.value.push(data as UserTalkDTO);
    });

    socket.value.on(DrawEnum.DRAW, (data) => {
      drawHistory.value = data.data as DrawHistory[];
      emitter.emit("refreshCanvas");
    });

    socket.value.on(DrawEnum.START_GAME, (data) => {
      roomStatus.value = data;
      if (roomStatus.value.startGameId !== -1) {
        roomStatus.value.seconds = Date.now() + roomStatus.value.seconds * 1000;
      }
      emitter.emit("refreshCanvas");
      localStorage.setItem('onlineUsersIn', JSON.stringify(onlineUsers.value.in))
      localStorage.setItem('onlineUsersOut', JSON.stringify(onlineUsers.value.out))
    });

    socket.value.on(DrawEnum.NEXT_ROUND, (data) => {
      roomStatus.value = data;
      if (roomStatus.value.startGameId !== -1) {
        roomStatus.value.seconds = Date.now() + roomStatus.value.seconds * 1000;
      }
      drawHistory.value = [];
      emitter.emit("refreshCanvas");
    });

    socket.value.on(DrawEnum.REFRESH_ROOM_STATUS, (data) => {
      roomStatus.value = data;
      if (roomStatus.value.startGameId !== -1) {
        roomStatus.value.seconds = Date.now() + roomStatus.value.seconds * 1000;
      }
    });

    socket.value.on(DrawEnum.GUESS_CORRECT, (correctUser: UserDTO) => {
      emitter.emit("guessCorrect", correctUser);
    });

    socket.value.on(DrawEnum.GAME_OVER, () => {
      emitter.emit("gameOver", roomStatus.value);
      roomStatus.value = {
        startGameId: -1,
        round: -1,
        room: "",
        seconds: -1,
        roomUserList: [],
        drawTitle: {
          id: -1,
          title: "",
        },
      };
      localStorage.removeItem('onlineUsersIn')
      localStorage.removeItem('onlineUsersOut')
    });

    socket.value.on(DrawEnum.REFRESH_CANVAS_IMAGE, () => {
      emitter.emit("refreshCanvasImage");
    });

    socket.value.on("connect_error", (error) => {
      // console.error("Socket.IO错误:", error, error.message);
    });

    socket.value.on("disconnect", () => {
      console.log("已断开服务器");
    });
    await getRoomStatus(roomName);
    getOnlineUsers()
  }

  function getOnlineUsers() {
    socket.value?.emit(DrawEnum.GET_ONLINE_USERS);
  }

  function talkEveryOne(event: UserTalkDTO, callback?: Function) {
    socket.value?.emit(DrawEnum.TALK_EVERYONE, event, () => {
      callback && callback();
    });
  }

  function draw(drawHistory: DrawHistory[]) {
    socket.value?.emit(DrawEnum.DRAW, {
      name: DrawEnum.DRAW,
      data: drawHistory,
    });
  }

  function seatDown(seat: number) {
    socket.value?.emit(DrawEnum.SEAT_DOWN, {
      name: DrawEnum.SEAT_DOWN,
      data: seat,
    });
    curSeat.value = seat;
  }

  function standUp() {
    socket.value?.emit(DrawEnum.STAND_UP, {
      name: DrawEnum.STAND_UP,
    })
    curSeat.value = null
  }

  function startGame(roomName: string) {
    socket.value?.emit(DrawEnum.START_GAME, {
      name: DrawEnum.START_GAME,
      data: {
        roomName,
      },
    });
  }

  function startGamePerm() {
    // 第一个位置的人 有权限开始游戏
    return curSeat.value != null && curSeat.value === 0;
  }

  function isStart() {
    return roomStatus.value !== null;
  }

  async function getRoomStatus(roomName: string) {
    const { data: res } = await getRoomStatusApi(roomName);
    roomStatus.value = res.data;
    if (roomStatus.value.startGameId !== -1) {
      roomStatus.value.seconds = Date.now() + roomStatus.value.seconds * 1000;
    }
  }

  function nextRound() {
    const params = {
      round: roomStatus.value.round,
      seat: roomStatus.value.roomUserList[roomStatus.value.round].position,
      startGameId: roomStatus.value.startGameId,
      imageUrl: '', // 预热
    }
    socket.value?.emit(DrawEnum.NEXT_ROUND, params);
    return params
  }

  function leaveRoom() {
    socket.value?.close()
  }

  function sendAllRefreshCanvasImage() {
    socket.value?.emit(DrawEnum.REFRESH_CANVAS_IMAGE, {
      name: DrawEnum.REFRESH_CANVAS_IMAGE,
      data: null,
    });
  }

  return {
    init,
    leaveRoom,
    socket,
    getOnlineUsers,
    onlineUsers,
    talkEveryOne,
    messageList,
    draw,
    drawHistory,
    isMobile,
    seatDown,
    curSeat,
    startGamePerm,
    startGame,
    isStart,
    roomStatus,
    nextRound,
    standUp,
    sendAllRefreshCanvasImage
  };
});
