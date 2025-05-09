import type { User } from "@/types";
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", () => {
  const user = ref<User>({});

  function isMe(id: number) {
    return user.value.id && user.value.id === id;
  }

  return { user, isMe };
});
