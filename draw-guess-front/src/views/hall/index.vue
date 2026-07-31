<script setup lang="ts">
import { getDrawRecommendApi, getOnlineRoom } from '@/api/draw';
import { getProfileApi, getRoomUserInfoBatchApi, wxLogin } from '@/api/user';
import { useGlobalStore } from '@/store/globalStore';
import { useUserStore } from '@/store/userStore';
import { Constant, type GameRound, type UserUnDTO } from '@/types';
import { formatDateTimeNoYear } from '@/utils';
import { message, Modal } from 'ant-design-vue';
import { Icon } from '@iconify/vue'
import Cookies from 'js-cookie'
import NavBar from '@/views/navbar/index.vue'

const isLoggedIn = computed(() => !!userStore.user.id)

const historyRoom = ref<UserUnDTO[]>([])
const onlineRoom = ref<{
  roomName: string
  roomUserList: Record<string, any>[]
}[]>([])
const historyTimeMap = ref<Record<string, Date>>({})
const router = useRouter()
const route = useRoute()
const { code } = route.query
const userStore = useUserStore()
const globalStore = useGlobalStore()
const recommendList = ref<GameRound[]>([])

async function initHistoryRoom() {
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

  if (isLoggedIn.value) {
    router.push({
      name: 'room',
      query: {
        roomName: userStore.user.username
      }
    })
  } else {
    window.$message.warning('未登录，请先登录')
    router.push({ name: 'profile' })
  }
}

function enterPublicRoom() {
  if (!isLoggedIn.value) {
    window.$message.warning('未登录，请先登录')
    router.push({ name: 'profile' })
    return
  }
  router.push({
    name: 'room',
    query: {
      roomName: 'public'
    }
  })
}

function enterRoom(roomName: string) {
  if (!isLoggedIn.value) {
    window.$message.warning('未登录，请先登录')
    router.push({ name: 'profile' })
    return
  }
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

function clearAllHistory() {
  Modal.confirm({
    title: '确认清空',
    content: '将清空所有历史进入的房间记录，确定要这么做吗？',
    okText: '确定清空',
    cancelText: '取消',
    okType: 'danger',
    centered: true,
    onOk() {
      localStorage.removeItem(Constant.HISTORY_ROOM)
      historyRoom.value = []
      historyTimeMap.value = {}
    },
  })
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

function roomDisplayName(room: typeof onlineRoom.value[number]): string {
  const firstUser = room.roomUserList[0]
  return firstUser ? `${firstUser.user.nickname}的房间` : room.roomName
}

function isRoomGaming(room: typeof onlineRoom.value[number]): boolean {
  return room.roomUserList.some((u: Record<string, any>) => u.score > 0)
}

let timer: number
async function initOnlineRoom() {
  const res = await getOnlineRoom()
  if (res.data) {
    onlineRoom.value = res.data.data
  }
}

async function judgeWxLogin() {
  let curCode = code
  if (!curCode) {
    const urlSearch = new URLSearchParams(window.location.search)
    const cd = urlSearch.get('code')
    if (cd) {
      curCode = cd
    }
  }
  if (curCode) {
    const token = Cookies.get(Constant.JWT_HEADER_NAME)
    if (token) return
    await wxLogin(curCode as string)
    const { data: res } = await getProfileApi()
    userStore.user = res.data
    message.success('微信授权登录成功')
  }
}

onMounted(() => {
  judgeWxLogin()
  initHistoryRoom()
  initOnlineRoom()
  timer = setInterval(() => {
    initOnlineRoom()
  }, 1000 * 10)
  getDrawRecommend()
})

onBeforeUnmount(() => {
  clearInterval(timer)
})

</script>

<template>
  <div class="w-full flex flex-col items-center min-h-screen bg-gradient-to-b from-gray-50 to-white">
    <div class="w-full max-w-[680px] relative px-4 py-6">
      <!-- 标题区 -->
      <a-affix :offset-top="0">
        <div class="text-center py-2 bg-gray-50">
          <h1 class="text-4xl font-extrabold tracking-tight">
            <span class="bg-gradient-to-r from-purple-600 via-pink-500 to-orange-400 bg-clip-text text-transparent">
              你画我猜
            </span>
          </h1>
          <p class="text-sm text-gray-400 mt-1 tracking-widest font-mono">2026</p>
        </div>
      </a-affix>

      <!-- 入口按钮 -->
      <div class="flex flex-col items-center justify-center gap-4 py-6 flex-wrap">
        <button
          class="flex cursor-pointer items-center gap-2 px-8 py-3 rounded-xl border-2 border-purple-200 text-purple-600 font-medium text-lg bg-white transition-all duration-200 hover:border-purple-400 hover:bg-purple-50 hover:shadow-md active:scale-95"
          @click="enterPublicRoom">
          <Icon icon="material-symbols:public" class="text-xl" />
          公共房间
        </button>
        <RainbowButton class="text-white text-lg px-10 py-3" @click="enterMyRoom">
          <span class="openmoji--house"></span>
          <span class="mx-2">我的房间</span>
          <span class="openmoji--house"></span>
        </RainbowButton>
      </div>

      <!-- 在线房间 -->
      <div v-if="onlineRoom.length > 0" class="mt-4">
        <h2 class="text-lg font-semibold flex items-center gap-2 mb-3 text-gray-700">
          <Icon icon="material-symbols:groups-outline" class="text-xl text-gray-400" />
          在线房间
        </h2>
        <div class="flex flex-col gap-2 max-h-80 overflow-y-auto pr-1 history-scroll">
          <div v-for="room in onlineRoom" :key="room.roomName" @click="enterRoom(room.roomName)"
            class="flex items-center gap-4 p-4 bg-white rounded-xl cursor-pointer border border-gray-100 transition-all duration-200 hover:border-gray-200 hover:bg-gray-50">
            <!-- 玩家头像组 -->
            <div class="flex -space-x-2 shrink-0">
              <a-avatar v-for="(u, i) in room.roomUserList.slice(0, 4)" :key="i" :src="u.user.avatar" :size="36"
                class="border-2 border-white" />
              <div v-if="room.roomUserList.length > 4"
                class="size-9 rounded-full bg-gray-100 border-2 border-white flex items-center justify-center text-xs text-gray-500 z-10">
                +{{ room.roomUserList.length - 4 }}
              </div>
            </div>
            <!-- 房间信息 -->
            <div class="min-w-0 flex-1">
              <p class="font-medium text-gray-800 truncate">{{ roomDisplayName(room) }}</p>
              <p class="text-xs text-gray-400 mt-0.5">
                {{ room.roomUserList.length }} 人在线
                <span v-if="isRoomGaming(room)"
                  class="inline-flex items-center ml-1.5 px-1.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
                  游戏中
                </span>
              </p>
            </div>
            <Icon icon="material-symbols:chevron-right" class="text-gray-300 shrink-0" />
          </div>
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="border-t border-gray-100 my-2"></div>

      <!-- 历史房间 -->
      <div class="mt-6">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-lg font-semibold flex items-center gap-2 text-gray-700">
            <Icon icon="material-symbols:history" class="text-xl text-gray-400" />
            历史进入的房间
          </h2>
          <button v-if="historyRoom.length > 0"
            class="flex items-center gap-1 text-xs text-gray-400 hover:text-red-500 transition-colors duration-200 cursor-pointer"
            @click="clearAllHistory">
            <Icon icon="material-symbols:delete-sweep-outline" class="text-base" />
            清空
          </button>
        </div>

        <!-- 空状态 -->
        <div v-if="historyRoom.length === 0"
          class="flex flex-col items-center justify-center py-12 text-gray-400 bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
          <Icon icon="material-symbols:door-back-outline" class="text-5xl mb-3 text-gray-300" />
          <p class="text-sm">还没有进入过任何房间</p>
          <p class="text-xs text-gray-300 mt-1">点击上方按钮创建或加入房间</p>
        </div>

        <!-- 房间列表 -->
        <TransitionGroup v-else name="room-list" tag="div"
          class="flex flex-col gap-2 max-h-96 overflow-y-auto pr-1 history-scroll">
          <div v-for="item in historyRoom" :key="item.username" @click="enterRoom(item.username)"
            class="group relative flex items-center gap-4 p-4 bg-white rounded-xl cursor-pointer border border-gray-100 transition-all duration-200 hover:border-gray-200 hover:bg-gray-50">
            <!-- 删除按钮 -->
            <button class="absolute right-1 top-1 size-6 flex items-center justify-center rounded-full bg-gray-200 text-gray-500 text-xs leading-none 
              transition-all duration-200 hover:bg-red-500 hover:text-white cursor-pointer"
              :class="globalStore.isMobile ? '' : 'opacity-0 group-hover:opacity-100'"
              @click.stop="removeRoom(item.username)" title="移除记录">
              ×
            </button>

            <a-avatar :src="item.avatar" :size="44" class="shrink-0"></a-avatar>
            <div class="min-w-0 flex-1">
              <p class="font-medium text-gray-800 truncate">{{ item.nickname }}的房间</p>
              <p class="text-xs text-gray-400 mt-0.5">
                {{ formatDateTimeNoYear(historyTimeMap[item.username]) }}
              </p>
            </div>
            <Icon icon="material-symbols:chevron-right" class="text-gray-300 shrink-0" />
          </div>
        </TransitionGroup>
      </div>

      <!-- 分隔线 -->
      <div class="border-t border-gray-100 my-6"></div>

      <div>
        <h2 class="text-lg font-semibold flex items-center gap-2 mb-3 text-gray-700">
          <Icon icon="material-symbols:draw" class="text-xl text-gray-400" />
          最近作品
        </h2>

        <!-- 空状态 -->
        <div v-if="!recommendList || recommendList.length === 0"
          class="flex flex-col items-center justify-center py-12 text-gray-400 bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
          <Icon icon="material-symbols:imagesmode-outline" class="text-5xl mb-3 text-gray-300" />
          <p class="text-sm">还没有推荐作品</p>
          <p class="text-xs text-gray-300 mt-1">完成画作后将会出现在这里</p>
        </div>

        <!-- 作品网格 -->
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 gap-3">
          <div v-for="item in recommendList" :key="item.imageUrl"
            class="aspect-square rounded-xl overflow-hidden bg-white border border-gray-100 shadow-sm cursor-pointer transition-all duration-300 hover:scale-[1.03] hover:shadow-lg hover:border-gray-200"
            @click="download(item.imageUrl)">
            <img class="w-full h-full object-cover" :src="item.imageUrl" alt="推荐作品" loading="lazy">
          </div>
        </div>
      </div>

      <!-- 底部留白（为固定导航条留空间） -->
      <div class="h-16"></div>
    </div>
  </div>
  <NavBar />
</template>

<style scoped>
/* TransitionGroup: 房间列表动画 */
.room-list-enter-active {
  transition: all 0.35s ease-out;
}

.room-list-leave-active {
  transition: all 0.25s ease-in;
}

.room-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.room-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.room-list-move {
  transition: transform 0.3s ease;
}

/* 自定义滚动条 */
.history-scroll {
  scrollbar-width: thin;
  scrollbar-color: #e5e7eb transparent;
}

.history-scroll::-webkit-scrollbar {
  width: 5px;
}

.history-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.history-scroll::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 10px;
}

.history-scroll::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}
</style>