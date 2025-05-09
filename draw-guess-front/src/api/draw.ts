import type { R, RoomStatus, StartGame } from "@/types";
import client from "@/utils/request";
import type { AxiosResponse } from "axios";

export function getRoomStatusApi(
  room: string
): Promise<AxiosResponse<R<RoomStatus>>> {
  return client.get("/draw/room/status?room=" + room, {
    headers: {
      isToken: true,
    },
  });
}

export function saveDrawApi(base64: string): Promise<AxiosResponse<R<string>>> {
  return client.post(
    "/upload/base64",
    {
      url: base64,
    },
    {
      headers: {
        isToken: true,
      },
    }
  );
}

export function getRoomLastRecordApi(
  room: string
): Promise<AxiosResponse<R<StartGame>>> {
  return client.get("/draw/room/lastRecord?room=" + room, {
    headers: {
      isToken: true,
    },
  });
}