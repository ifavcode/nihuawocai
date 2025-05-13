<script setup lang="ts">
import { getRoomUserInfoApi } from '@/api/user';
import { useGlobalStore } from '@/store/globalStore';
import type { UserDTO } from '@/types';
import { storeToRefs } from 'pinia';


const route = useRoute()
const { roomName } = route.query
const roomUser = ref<Partial<UserDTO>>({})
const loading = ref(false)

const globalStore = useGlobalStore()
const { onlineUsers } = storeToRefs(globalStore)

function getRoomNumber() {
  let cnt = onlineUsers.value.in.reduce((pre, cur) => {
    pre += cur.user.nickname !== '加入' ? 1 : 0
    return pre
  }, 0)
  cnt += onlineUsers.value.out.length
  return cnt
}

async function getRoomUserInfo() {
  try {
    loading.value = true
    const { data: res } = await getRoomUserInfoApi((roomName as string) || '')
    if (res.data) {
      roomUser.value = res.data
    }
  } catch (error) {

  } finally {
    loading.value = false
  }
}

getRoomUserInfo()

</script>


<template>
  <header
    class="absolute top-0 left-0 right-0 w-full h-20 bg-gradient-to-r from-orange-500 via-orange-400 to-orange-500">
    <div class="p-4">
      <div v-if="loading">
        <p class="svg-spinners--ring-resize text-white"></p>
      </div>
      <div v-else class="flex justify-between">
        <div>
          <p class="text-white font-semibold text-lg">{{ roomUser.nickname }}的房间</p>
          <p class="text-white text-sm">
            <span class="">{{ getRoomNumber() }}</span>
            人在线
          </p>
        </div>
        <a-avatar :src="roomUser.avatar" style="background: #fff;" :size="40" />
      </div>
    </div>
  </header>
</template>