import type { R, User } from "@/types";
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
