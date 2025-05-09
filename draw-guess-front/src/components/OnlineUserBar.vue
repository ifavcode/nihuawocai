<script setup lang="ts">
import { useGlobalStore } from '@/store/globalStore';
import { useUserStore } from '@/store/userStore';
import type { RoomUserDTO, UserDTO } from '@/types';
import { emitter } from '@/utils';
import { storeToRefs } from 'pinia';
import confetti from 'canvas-confetti'

const globalStore = useGlobalStore()
const userStore = useUserStore()
const canJoin = function (user: UserDTO) {
  return user.nickname === '加入'
}

function havePerson() {
  return (onlineUsers.value.in.length + onlineUsers.value.out.length) !== 0
}

function getRoomNumber() {
  let cnt = onlineUsers.value.in.reduce((pre, cur) => {
    pre += cur.user.nickname !== '加入' ? 1 : 0
    return pre
  }, 0)
  cnt += onlineUsers.value.out.length
  return cnt
}

function seatDown(roomUserDTO: RoomUserDTO, seat: number) {
  if (!canJoin(roomUserDTO.user)) return
  globalStore.seatDown(seat)
}

const { onlineUsers } = storeToRefs(globalStore)
const userRef = ref<HTMLElement[]>([])

function guessCorrect(correctUser: UserDTO) {
  for (let i = 0; i < globalStore.roomStatus.roomUserList.length; i++) {
    if (globalStore.roomStatus.roomUserList[i].user.id === correctUser.id) {
      const rect = userRef.value[i].getBoundingClientRect();
      const x = rect.left + rect.width / 2;
      const y = rect.top + rect.height / 2;
      confetti({
        particleCount: 50,
        spread: 10,
        origin: {
          x: x / window.innerWidth,
          y: y / window.innerHeight
        }
      });
      break
    }
  }
}

emitter.on('guessCorrect', guessCorrect)

</script>

<template>
  <div class="w-full max-w-[680px]">
    <p v-show="havePerson" class="text-sm mb-2 px-4 text-gray-600">房间共加入{{ getRoomNumber() }}人</p>
    <div class="overflow-x-auto flex gap-4">
      <div v-for="(item, index) in onlineUsers.in" ref="userRef" class="w-16 flex flex-col items-center gap-1 relative"
        @click="seatDown(item, index)"
        :class="[canJoin(item.user) ? 'cursor-pointer group' : '', globalStore.roomStatus.startGameId !== -1 && canJoin(item.user) ? 'hidden' : '']">
        <a-avatar :src="item.user.avatar" :size="40"
          :style="{ border: globalStore.roomStatus.round === index ? 'solid 1px #00c950' : 'none' }" />
        <p class="w-full line-clamp-2 text-sm text-center"
          :class="canJoin(item.user) ? 'text-gray-500 group-hover:text-primary text-green' : ''">{{
            item.user.nickname }}</p>
        <div v-show="userStore.isMe(item.user.id)"
          class="absolute top-[-4px] left-[50%] translate-x-[-50%] text-xs text-black font-semibold" title="这是你自己">
          YOU
        </div>
        <div v-show="globalStore.roomStatus.startGameId !== -1"
          class="absolute top-0 right-0 text-xs text-red-500 font-semibold">
          {{ globalStore.roomStatus.roomUserList[index]?.score || 0 }}
        </div>
      </div>
      <p class="text-sm" v-show="!havePerson">没有用户在线</p>
    </div>
    <div v-show="havePerson">
      <p class="text-sm mb-2 px-4 text-gray-600 mt-4">等待入座</p>
      <div class="flex gap-4">
        <div v-for="(item) in onlineUsers.out" class="w-16 flex flex-col items-center gap-1 relative">
          <a-avatar :src="item.user.avatar" :size="40" />
          <p class="w-full line-clamp-2 text-sm text-center">{{ item.user.nickname }}</p>
          <div v-show="userStore.isMe(item.user.id)"
            class="absolute top-[-4px] left-[50%] translate-x-[-50%] text-xs text-black font-semibold" title="这是你自己">
            YOU
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
