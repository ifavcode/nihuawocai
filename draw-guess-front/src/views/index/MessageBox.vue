<script setup lang="ts">
import { useGlobalStore } from '@/store/globalStore';
import { useUserStore } from '@/store/userStore';
import type { UserDTO, UserTalkDTO } from '@/types';
import { emitter } from '@/utils';
import dayjs from 'dayjs';
import { nanoid } from 'nanoid';
import { storeToRefs } from 'pinia';

const globalStore = useGlobalStore()
const { messageList } = storeToRefs(globalStore)
const userStore = useUserStore()

const visibleMessages = ref<UserTalkDTO[]>([])

let timer: number
function refresh() {
  clearTimeout(timer)
  visibleMessages.value = messageList.value.filter(msg => {
    const now = dayjs();
    const target = dayjs(msg.createTime);
    return now.diff(target, "second") < 5;
  })
  timer = setTimeout(() => {
    refresh()
  }, 1000);
}

function guessCorrect(correctUser: UserDTO) {
  messageList.value.push({
    id: nanoid(8),
    userId: -1,
    nickname: 'system',
    avatar: '',
    content: `🎉恭喜${correctUser.nickname}猜对了答案!`,
    createTime: new Date()
  })
  refresh()
  
}

watch(globalStore.messageList, () => {
  refresh()
})

defineExpose({ refresh })

onMounted(() => {
  refresh()
})

onUnmounted(() => {
  // 清理定时器
  clearTimeout(timer)
})

emitter.on('guessCorrect', guessCorrect)

</script>

<template>
  <div class="px-2 h-40 w-40 max-w-full flex flex-col gap-2">
    <TransitionGroup name="message" enter-active-class="animate-enter" leave-active-class="animate-leave">
      <div v-for="message in visibleMessages" class="flex items-center gap-2 w-full" :key="message.id">
        <a-avatar v-if="message.avatar" :src="message.avatar" :size="32" />
        <p v-if="message.nickname != 'system'" class="text-sm  px-2 py-1 rounded-md text-black"
          :class="userStore.isMe(message.userId) ? 'bg-green-400' : 'bg-white'">{{
            message.content }}</p>
        <p v-else class="text-sm px-2 py-1 rounded-md text-green-500">
          {{ message.content }}
        </p>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.animate-enter {
  animation: slide-in 0.3s ease-out;
}

.animate-leave {
  animation: slide-out 0.3s ease-in;
}

@keyframes slide-in {
  from {
    transform: translateX(-10px);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slide-out {
  from {
    transform: translateX(0);
    opacity: 1;
  }

  to {
    transform: translateX(10px);
    opacity: 0;
  }
}
</style>