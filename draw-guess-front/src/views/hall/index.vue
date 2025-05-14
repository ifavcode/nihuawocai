<script setup lang="ts">
import { getDrawRecommendApi } from '@/api/draw';
import { getProfileApi, getRoomUserInfoBatchApi } from '@/api/user';
import { useGlobalStore } from '@/store/globalStore';
import { useUserStore } from '@/store/userStore';
import { Constant, type GameRound, type UserUnDTO } from '@/types';
import { autoRegisterAndLogin, formatDateTimeNoYear } from '@/utils';

const historyRoom = ref<UserUnDTO[]>([])
const historyTimeMap = ref<Record<string, Date>>({})
const router = useRouter()
const userStore = useUserStore()
const globalStore = useGlobalStore()
const recommendList = ref<GameRound[]>([])

async function autoLogin() {
  await autoRegisterAndLogin()
  const { data: res } = await getProfileApi()
  userStore.user = res.data
}

async function initHistoryRoom() {
  await autoLogin()
  const historyRoomStorage = localStorage.getItem(Constant.HISTORY_ROOM)
  if (historyRoomStorage) {
    const historyRoomTmp: Record<string, any>[] = JSON.parse(historyRoomStorage)
    for (let history of historyRoomTmp) {
      historyTimeMap.value[history.roomName] = history.time
    }

    const { data: res } = await getRoomUserInfoBatchApi(historyRoomTmp.reduce((pre, cur) => {
      pre.push(cur.roomName)
      return pre
    }, []) as string[])
    if (res.data) {
      historyRoom.value = res.data
    }
  }
}

function enterMyRoom() {
  if (userStore.user.username) {
    router.push({
      name: 'room',
      query: {
        roomName: userStore.user.username
      }
    })
  } else {
    window.$message.error('进入失败，请重试')
  }
}

function enterRoom(roomName: string) {
  router.push({
    name: 'room',
    query: {
      roomName
    }
  })
}

function removeRoom(roomName: string) {
  const historyRoomStorage = localStorage.getItem(Constant.HISTORY_ROOM)
  if (historyRoomStorage) {
    let historyRoomTmp: Record<string, any>[] = JSON.parse(historyRoomStorage)
    historyRoomTmp = historyRoomTmp.filter(v => v.roomName != roomName)
    localStorage.setItem(Constant.HISTORY_ROOM, JSON.stringify(historyRoomTmp))
  }
  initHistoryRoom()
}

async function getDrawRecommend() {
  const { data: res } = await getDrawRecommendApi()
  if (res.data) {
    recommendList.value = res.data
  }
}

function download(url: string) {
  window.open(url)
}

onMounted(() => {
  initHistoryRoom()
  getDrawRecommend()
})

</script>

<template>
  <div class="w-full flex flex-wrap flex-col items-center ">
    <div class="w-full max-w-[680px] relative p-4">
      <h1 class="font-semibold">
        <span class="text-2xl">你画我猜</span>
        <br />
        <span class="text-xl text-gray-800">2025</span>
      </h1>
      <div class="text-center py-8">
        <RainbowButton class="text-white" @click="enterMyRoom">
          <i class="openmoji--house"></i>我的房间<i class="openmoji--house"></i>
        </RainbowButton>
      </div>
      <h1 class="text-xl">历史进入的房间</h1>
      <div class="py-2">
        <p v-if="historyRoom.length === 0">空</p>
        <div v-else class="flex flex-col gap-2 max-h-96 overflow-y-auto">
          <div v-for="item in historyRoom" @click="enterRoom(item.username)"
            class="p-2 bg-white rounded-md flex gap-4 py-4 cursor-pointer hover:bg-gray-100 focus:bg-gray-200 relative group">
            <div class=" absolute right-4 top-2 hover:text-red-500  group-hover:block"
              @click.stop="removeRoom(item.username)" :class="globalStore.isMobile ? '' : 'hidden'">
              ×
            </div>
            <a-avatar :src="item.avatar" :size="40"></a-avatar>
            <div>
              <p>{{ item.nickname }}的房间</p>
              <p class="text-sm text-gray-500">{{ formatDateTimeNoYear(historyTimeMap[item.username]) }}</p>
            </div>
          </div>
        </div>
      </div>
      <h1 class="text-xl mt-8">作品推荐</h1>
      <div class="py-2 flex flex-wrap gap-4">
        <div v-for="item in recommendList" class="sm:size-48 max-sm:w-[45%]">
          <img class=" bg-white cursor-pointer" :src="item.imageUrl" alt="" @click="download(item.imageUrl)">
        </div>
      </div>
    </div>
  </div>
</template>