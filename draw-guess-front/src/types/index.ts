export enum Constant {
  JWT_HEADER_NAME = "drawauth",
  HISTORY_ROOM = "historyRoom"
}

export interface R<T> {
  code: number;
  message: string;
  data: T;
}

export interface DrawHistory {
  lineWidth: number;
  lineColor: string;
  points: Point[];
}

export interface Point {
  x: number;
  y: number;
}

export enum DrawEnum {
  GET_ONLINE_USERS = "get_online_users",
  TALK_EVERYONE = "talk_everyone",
  DRAW = "draw",
  SEAT_DOWN = "seat_down",
  START_GAME = "start_game",
  NEXT_ROUND = "next_round",
  GAME_OVER = "game_over",
  GUESS_CORRECT = "guess_correct",
  REFRESH_ROOM_STATUS = "refresh_room_status",
  STAND_UP = "stand_up"
}

export interface DrawEvent<T> {
  name: DrawEnum;
  data: T;
}

export interface UserDTO {
  id: number;
  nickname: string;
  avatar: string;
}

export interface UserUnDTO {
  id: number;
  nickname: string;
  username: string;
  avatar: string;
}

export interface RoomUserDTO {
  position: number;
  user: UserDTO;
  score: number;
}

export interface UserTalkDTO {
  id: string;
  userId: number;
  nickname: string;
  avatar: string;
  content: string;
  createTime: Date;
}

export interface RoomUserInOut {
  in: RoomUserDTO[];
  out: RoomUserDTO[];
}

export interface User {
  id?: number;
  username?: string;
  nickname?: string;
  password?: string;
  avatar?: string;
  createTime?: any;
}

export interface GameRoundDTO {
  round: number;
  seat: number;
  startGameId: number;
  imageUrl: string;
}

export interface StartGameDTO {
  roomName: string;
}

export interface RoomStatus {
  startGameId: number;
  round: number;
  room: string;
  seconds: number;
  roomUserList: RoomUserDTO[];
  drawTitle: DrawTitle;
}

export interface StartGame {
  id: number;
  user: User;
  roomName: string;
  joinedCount: number;
  creteTime: any;
  gameRoundList: GameRound;
}

export interface GameRound {
  id: number;
  startGame?: StartGame;
  round: number;
  seat: number;
  imageUrl: string;
  user?: User;
  createTime: any;
  drawTitle: DrawTitle;
}

export interface DrawTitle {
  id: number;
  title: string;
}
