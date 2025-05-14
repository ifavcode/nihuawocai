import { type ClassValue, clsx } from "clsx";
import dayjs from "dayjs";
import { twMerge } from "tailwind-merge";

import mitt from "mitt";
import type { RoomStatus, User, UserDTO } from "@/types";
import { loginApi, registerApi } from "@/api/user";
import { nanoid } from "nanoid";

type Events = {
  refreshCanvas: void;
  gameOver: RoomStatus;
  refreshCanvasImage: void;
  guessCorrect: UserDTO;
  testEvent: void;
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

export { emitter, formatDateTime, formatDate, cn, autoRegisterAndLogin, formatDateTimeNoYear };
