import type { R, User, UserDTO, UserUnDTO } from "@/types";
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

