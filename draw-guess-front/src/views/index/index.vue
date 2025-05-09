<script setup lang="ts">
import { useGlobalStore } from '@/store/globalStore'
import { DrawEnum, type DrawHistory, type RoomStatus, type RoomUserDTO, type StartGame } from '@/types'
import { nanoid } from 'nanoid'
import type MessageBox from './MessageBox.vue'
import { storeToRefs } from 'pinia'
import { emitter } from '@/utils'
import { useUserStore } from '@/store/userStore'
import { getRoomLastRecordApi, saveDrawApi } from '@/api/draw'
import { Modal } from 'ant-design-vue'

const route = useRoute()
const roomName = route.query.roomName ? route.query.roomName as string : 'public'
const drawBoardRef = ref<HTMLCanvasElement>()
const drawBoardWrap = ref<HTMLElement>()
const globalStore = useGlobalStore()
const userStore = useUserStore()
const inputValue = ref('')
const colors = ref([
  "#FF0000", // 红色
  "#FFA500", // 橙色
  "#FFFF00", // 黄色
  "#008000", // 绿色
  "#0000FF", // 蓝色
  "#4B0082", // 靛色
  "#EE82EE", // 紫色
  "#FFC0CB", // 粉色
  "#A52A2A", // 棕色
  "#808080", // 灰色
  "#FFFFFF", // 白色
  "#000000"  // 黑色
])
const drawStatus = reactive({
  isDrawing: false,
  lastX: 0,
  lastY: 0,
  lineColor: '#000000',
  lineWidth: 2,
  chooseColorPop: false
})
const { drawHistory, isMobile } = storeToRefs(globalStore)
const { user } = storeToRefs(userStore)

const canDraw = ref(false) // 当前是否轮到自己画画

// 同步别人的
watch(globalStore.drawHistory, () => {
  globalStore.draw(drawHistory.value)
})

globalStore.$subscribe(() => {
  let oldValue = canDraw.value
  canDraw.value =
    globalStore.roomStatus.startGameId !== -1 &&
    globalStore.roomStatus.roomUserList[globalStore.roomStatus.round].user.id === userStore.user.id;
  let newValue = canDraw.value
  if (!oldValue && newValue) {
    // 轮到我画画了😊
    Modal.info({
      title: '轮到你画画了',
      content: h('div', {}, [
        h('p', `题目：${globalStore.roomStatus.drawTitle.title}`),
      ]),
    });
  }
});

function startDraw() {
  if (drawBoardRef.value) {
    drawBoardRef.value.width = drawBoardWrap.value?.clientWidth || 480
    drawBoardRef.value.height = 600
    const ctx: CanvasRenderingContext2D = drawBoardRef.value.getContext('2d') as CanvasRenderingContext2D
    ctx.fillStyle = '#fff'
    if (isMobile.value) {
      drawBoardRef.value.addEventListener('touchstart', (e) => {
        e.preventDefault(); // 阻止默认触摸行为，防止页面滚动
        if (!canDraw.value) return
        drawStatus.isDrawing = true;
        const touch = e.touches[0];
        const rect = drawBoardRef.value!.getBoundingClientRect();
        ctx.beginPath();
        [drawStatus.lastX, drawStatus.lastY] = [touch.clientX - rect.left, touch.clientY - rect.top];
        drawHistory.value.push({
          lineColor: drawStatus.lineColor,
          lineWidth: drawStatus.lineWidth,
          points: []
        });
        // 同步自己的
        globalStore.draw(drawHistory.value);
      });

      drawBoardRef.value.addEventListener('touchmove', (e) => {
        if (drawStatus.isDrawing) {
          e.preventDefault(); // 阻止默认触摸行为，防止页面滚动
          if (!canDraw.value) return
          const touch = e.touches[0];
          const rect = drawBoardRef.value!.getBoundingClientRect();
          ctx.strokeStyle = drawStatus.lineColor;
          ctx.lineWidth = drawStatus.lineWidth;
          ctx.moveTo(drawStatus.lastX, drawStatus.lastY);
          const currentX = touch.clientX - rect.left;
          const currentY = touch.clientY - rect.top;
          ctx.lineTo(currentX, currentY);
          ctx.stroke();
          [drawStatus.lastX, drawStatus.lastY] = [currentX, currentY];
          const currentPath = drawHistory.value[drawHistory.value.length - 1];
          currentPath.points.push({
            x: currentX,
            y: currentY
          });
          // 同步自己的
          globalStore.draw(drawHistory.value);
        }
      });
      drawBoardRef.value.addEventListener('touchend', () => {
        drawStatus.isDrawing = false;
      });
    } else {
      drawBoardRef.value.addEventListener('mousedown', (e: MouseEvent) => {
        if (!canDraw.value) return
        drawStatus.isDrawing = true;
        ctx.beginPath();
        [drawStatus.lastX, drawStatus.lastY] = [e.offsetX, e.offsetY];
        drawHistory.value.push({
          lineColor: drawStatus.lineColor,
          lineWidth: drawStatus.lineWidth,
          points: []
        })
        // 同步自己的
        globalStore.draw(drawHistory.value)
      });

      drawBoardRef.value.addEventListener('mousemove', (e) => {
        if (!canDraw.value) return
        if (drawStatus.isDrawing) {
          ctx.strokeStyle = drawStatus.lineColor
          ctx.lineWidth = drawStatus.lineWidth
          ctx.moveTo(drawStatus.lastX, drawStatus.lastY);
          ctx.lineTo(e.offsetX, e.offsetY);
          ctx.stroke();
          [drawStatus.lastX, drawStatus.lastY] = [e.offsetX, e.offsetY];
          drawHistory.value[drawHistory.value.length - 1].points.push({
            x: e.offsetX,
            y: e.offsetY
          })
          // 同步自己的
          globalStore.draw(drawHistory.value)
        }
      });

      drawBoardRef.value.addEventListener('mouseup', (e) => {
        drawStatus.isDrawing = false;
      });

      drawBoardRef.value.addEventListener('mouseout', () => {
        drawStatus.isDrawing = false;
      });
    }
  } else {
    console.warn('失败');
  }
}

function chooseColor(color: string) {
  drawStatus.lineColor = color
  drawStatus.chooseColorPop = false
}

function chooseEraser() {
  drawStatus.lineColor = '#ffffff'
}

function clearDraw() {
  if (drawBoardRef.value) {
    const ctx: CanvasRenderingContext2D = drawBoardRef.value.getContext('2d') as CanvasRenderingContext2D
    ctx.clearRect(0, 0, drawBoardRef.value.width, drawBoardRef.value.height)
    drawHistory.value = []
    globalStore.draw(drawHistory.value);
  } else {
    console.warn('失败');
  }
}

function backDraw() {
  if (drawBoardRef.value && drawHistory.value.length > 0) {
    const ctx: CanvasRenderingContext2D = drawBoardRef.value.getContext('2d') as CanvasRenderingContext2D
    ctx.clearRect(0, 0, drawBoardRef.value.width, drawBoardRef.value.height)
    drawHistory.value.pop()
    refreshCanvas(ctx)
    globalStore.draw(drawHistory.value);
  } else {
    console.warn('失败');
  }
}

function refreshCanvas(ctx?: CanvasRenderingContext2D, clear: boolean = false) {
  if (!drawBoardRef.value) return
  if (!ctx) {
    ctx = drawBoardRef.value.getContext('2d') as CanvasRenderingContext2D
  }
  if (clear) {
    ctx.clearRect(0, 0, drawBoardRef.value.width, drawBoardRef.value.height)
  }
  drawHistory.value.forEach(path => {
    ctx.strokeStyle = path.lineColor;
    ctx.lineWidth = path.lineWidth;
    ctx.beginPath();
    path.points.forEach((point, index) => {
      if (index === 0) {
        ctx.moveTo(point.x, point.y);
      } else {
        ctx.lineTo(point.x, point.y);
      }
    });
    ctx.stroke();
  });
}

function refreshCanvasFunc() {
  refreshCanvas(undefined, true)
}

const messageBoxRef = ref<InstanceType<typeof MessageBox>>()
function sendMessage() {
  if (inputValue.value === '') return
  globalStore.talkEveryOne({
    id: nanoid(8),
    userId: user.value.id as number,
    nickname: user.value.nickname || '匿名用户',
    avatar: user.value.avatar || 'https://robohash.org/HelloWorld?set=set1&bgset=bg1&size=200x200',
    content: inputValue.value,
    createTime: new Date()
  }, () => {
    inputValue.value = ''
  })
}

function startGame() {
  if (user.value.username) {
    globalStore.startGame(user.value.username)
    curGame.value = {}
    drawHistory.value = []
  } else {
    window.$message.error('似乎出现了一些预料之外的事，刷新试试吧')
  }
}

async function drawRoundEnd() {
  // 只需要画画者调用
  if (canDraw.value) {
    const base64 = drawBoardRef.value?.toDataURL('image/png')
    const imageUrl = await saveDraw(base64 as string)
    globalStore.nextRound(imageUrl)
  }
}

async function saveDraw(base64: string) {
  const { data: res } = await saveDrawApi(base64)
  return res.data
}

const curGame = ref<Partial<StartGame>>({})
async function getRoomLastRecord() {
  const { data: res } = await getRoomLastRecordApi(roomName)
  curGame.value = res.data
}

function gameOver(roomStatus: RoomStatus) {
  getRoomLastRecord()
  const roomUserList: RoomUserDTO[] = JSON.parse(JSON.stringify(roomStatus.roomUserList))
  roomUserList.sort((o1, o2) => o2.score - o1.score)
  Modal.info({
    title: '排行榜',
    content: h('div', {}, [
      h('p', `题目：${roomStatus.drawTitle.title}`),
      h('ul', roomUserList.map(user =>
        h('li', { class: 'flex items-center gap-2' }, [
          h('img', { src: user.user.avatar, class: 'size-10 rounded-full bg-gray-50' }),
          h('p', { class: '' }, `${user.user.nickname} —— ${user.score}分`),
        ])
      ))
    ]),
  });
}

onMounted(() => {
  startDraw()
  getRoomLastRecord()
})

emitter.on('refreshCanvas', () => {
  refreshCanvasFunc()
})
emitter.on('refreshCanvasImage', () => {
  getRoomLastRecord()
})
emitter.on('gameOver', (roomStatus) => {
  gameOver(roomStatus)
})
emitter.on('testEvent', () => {
  console.log('建立testEvent成功');
})

</script>

<template>
  <div class="w-full flex flex-wrap flex-col items-center ">
    <div class="w-full max-w-[680px] relative" ref="drawBoardWrap">
      <div class="w-full h-full absolute top-0 left-0 bg-white" title="等待开始游戏"
        v-if="globalStore.roomStatus.startGameId === -1">
      </div>
      <canvas ref="drawBoardRef" :class="canDraw ? 'cursor-crosshair' : ''" />
      <div class="absolute h-11 bottom-0 left-0 w-full p-2 flex items-center gap-4">
        <span class="iconoir--edit-pencil hover-primary cursor-pointer" title="画笔"></span>
        <span class="material-symbols--ink-eraser-outline hover-primary cursor-pointer" title="橡皮擦"
          @click="chooseEraser"></span>
        <a-popconfirm :open="drawStatus.chooseColorPop">
          <template #icon>
          </template>
          <template #cancelButton>
          </template>
          <template #okButton></template>
          <template #title>
            <div class="w-36 flex flex-wrap justify-around gap-1">
              <div v-for="color in colors" :style="{ background: color }"
                class="size-8 rounded-full border border-gray-200 cursor-pointer hover:border-gray-400"
                @click="chooseColor(color)"></div>
            </div>
          </template>
          <span class="material-symbols--palette-outline hover-primary cursor-pointer"
            :style="{ color: drawStatus.lineColor }" title="调色盘" @click="drawStatus.chooseColorPop = true"></span>
        </a-popconfirm>
        <span class="fluent--edit-arrow-back-24-regular hover-primary cursor-pointer" @click="backDraw"
          title="撤销"></span>
        <span class="material-symbols--delete-forever-outline-rounded hover-primary cursor-pointer" @click="clearDraw"
          title="清空"></span>
      </div>
      <div class="absolute bottom-11 left-0 pointer-events-none">
        <message-box ref="messageBoxRef" />
      </div>
      <div class="absolute left-[50%] translate-x-[-50%] bottom-16" v-if="globalStore.roomStatus.startGameId === -1">
        <!-- <shimmer-button shimmerColor="#fff" background="#000" shimmerSize="0.2rem">开始游戏</shimmer-button> -->
        <rainbow-button class="text-white" v-if="globalStore.startGamePerm()" @click="startGame">开始游戏</rainbow-button>
        <rainbow-button class="text-white cursor-not-allowed" v-else>等待首座玩家开始游戏</rainbow-button>
      </div>
      <div class="absolute top-4 right-4 pointer-events-none" v-if="globalStore.roomStatus.startGameId !== -1">
        <a-statistic-countdown :value="globalStore.roomStatus.seconds" @finish="drawRoundEnd" format="ss秒"
          :valueStyle="{ lineHeight: '16px' }">
          <template #title>
            <span>剩余时间</span>
          </template>
        </a-statistic-countdown>
      </div>
      <div class="absolute top-4 left-[50%] translate-x-[-50%] pointer-events-none text-center"
        v-if="globalStore.roomStatus.startGameId !== -1">
        <p class="text-sm">
          {{
            globalStore.roomStatus.roomUserList.find(v => v.position === globalStore.roomStatus.round)?.user.nickname
          }}
          正在画画
        </p>
        <p v-if="canDraw">
          {{ globalStore.roomStatus.drawTitle.title }}
        </p>
        <p v-else>
          {{ globalStore.roomStatus.drawTitle.title.length }}个字
        </p>
      </div>
    </div>
    <div class="w-full max-w-[680px] py-2 flex group">
      <a-input :bordered="false" placeholder="友善交流 ——回车发送" v-model:value="inputValue" @keydown.enter="sendMessage" />
      <!-- <div class="hidden group-focus-within:inline-block h-full">
        <ripple-button class="w-20 h-8" @click="sendMessage" disabled>发送</ripple-button>
      </div> -->
    </div>
    <online-user-bar />
    <div class="w-full max-w-[680px] p-4">
      <div class="w-full flex flex-col gap-4 ">
        <div v-if="curGame" v-for="item in curGame.gameRoundList" class="">
          <div class="mb-2 font-semibold text-primary">
            <p>{{ item.drawTitle?.title }}</p>
          </div>
          <div class="bg-white rounded-sm">
            <img :src="item.imageUrl" alt="">
          </div>
        </div>
      </div>
    </div>
  </div>
</template>