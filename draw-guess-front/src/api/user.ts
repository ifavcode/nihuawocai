import type { GameRound, R, User, UserDTO, UserUnDTO } from "@/types";
import client from "@/utils/request";
import type { AxiosResponse } from "axios";

export function registerApi(user: User) {
  return client.post("/system/register", user);
}

export function loginApi(user: User) {
  return client.post("/system/login", user);
}

export function getProfileApi(): Promise<AxiosResponse<R<User>>> {
  return client.get("/user/info", {
    headers: {
      isToken: true,
    },
  });
}

export function getRoomUserInfoApi(username: string): Promise<AxiosResponse<R<UserUnDTO>>> {
  return client.get("/user/username?username=" + username, {
    headers: {
      isToken: true,
    },
  });
}

export function getRoomUserInfoBatchApi(usernames: string[]): Promise<AxiosResponse<R<UserUnDTO[]>>> {
  return client.get("/user/usernameBatch?usernames=" + usernames.join(','), {
    headers: {
      isToken: true,
    },
  });
}

export function getDrawProfileApi(
): Promise<AxiosResponse<R<GameRound[]>>> {
  return client.get("/draw/profileRecently", {
    headers: {
      isToken: true,
    },
  });
}

export function updateProfileApi(
  dto: Omit<UserDTO, 'id'>
): Promise<AxiosResponse<R<GameRound[]>>> {
  return client.post("/user/updateProfile", dto, {
    headers: {
      isToken: true,
    },
  });
}

export function uploadFileApi(
  file: File
): Promise<AxiosResponse<R<string>>> {
  const formData = new FormData()
  formData.append('file', file)
  return client.post("/upload/file", formData, {
    headers: {
      isToken: true,
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function changePasswordApi(
  data: { rawPassword: string, newPassword: string }
): Promise<AxiosResponse<R<GameRound[]>>> {
  return client.post("/user/changePwd", data, {
    headers: {
      isToken: true,
    },
  });
}