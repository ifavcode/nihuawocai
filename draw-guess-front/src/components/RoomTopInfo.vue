<script setup lang="ts">
import { getRoomUserInfoApi } from '@/api/user';
import { useGlobalStore } from '@/store/globalStore';
import type { UserDTO } from '@/types';
import { storeToRefs } from 'pinia';
import copy from 'copy-to-clipboard'


const route = useRoute()
const { roomName } = route.query
const roomUser = ref<Partial<UserDTO>>({})
const loading = ref(false)

const globalStore = useGlobalStore()
const { onlineUsers } = storeToRefs(globalStore)
const router = useRouter()

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

function backHall() {
  globalStore.leaveRoom()
  router.push({
    name: 'hall'
  })
}

async function copyUrl() {
  copy(window.location.href)
  window.$message.success('房间地址已复制到剪贴板')
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
          <div class="flex gap-1 items-center">
            <i class="material-symbols--keyboard-backspace-rounded text-white hover:text-primary cursor-pointer"
              @click="backHall"></i>
            <p class="text-white font-semibold text-lg">{{ roomUser.nickname }}的房间</p>
            <i class="fluent--share-16-regular text-white hover:text-primary cursor-pointer" @click="copyUrl"></i>
          </div>
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