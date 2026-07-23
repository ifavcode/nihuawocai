import { type ClassValue, clsx } from "clsx";
import dayjs from "dayjs";
import { twMerge } from "tailwind-merge";
import Cookies from 'js-cookie'
import mitt from "mitt";
import { Constant, type RoomStatus, type User, type UserDTO } from "@/types";
import { getProfileApi, loginApi, registerApi } from "@/api/user";
import { nanoid } from "nanoid";
import { useUserStore } from "@/store/userStore";

type Events = {
  refreshCanvas: void;
  gameOver: RoomStatus;
  refreshCanvasImage: void;
  guessCorrect: UserDTO;
  testEvent: void;
  loginSuccess: void;
};
const emitter = mitt<Events>();

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export type ObjectValues<T> = T[keyof T];

function formatDate(date: Date) {
  return dayjs(date).format("YYYY-MM-DD");
}

function formatDateTime(date: Date) {
  return dayjs(date).format("YYYY-MM-DD HH:mm:ss");
}

function formatDateTimeNoYear(date: Date) {
  return dayjs(date).format("MM-DD HH:mm");
}

async function autoRegisterAndLogin() {
  const user: User = {
    username: nanoid(8),
    password: "123456", // 默认密码
  };
  try {
    await registerApi(user);
    await loginApi(user);
  } catch (error) {
    console.log(error);
  }
}

async function autoLogin() {
  const token = Cookies.get(Constant.JWT_HEADER_NAME)
  if (!token) return
  const { data: res } = await getProfileApi()
  if (res.data) {
    useUserStore().user = res.data
    nextTick(() => {
      emitter.emit('loginSuccess')
    })
  }
}

export { emitter, formatDateTime, formatDate, cn, autoRegisterAndLogin, formatDateTimeNoYear, autoLogin };
