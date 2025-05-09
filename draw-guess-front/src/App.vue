<script setup lang="ts">
import { RouterView } from 'vue-router';
import { useGlobalStore } from '@/store/globalStore'
import Cookies from 'js-cookie';
import { autoRegisterAndLogin } from './utils';
import { getProfileApi } from './api/user';
import { Constant } from './types';
import { useUserStore } from './store/userStore';
import type { MessageApi } from 'ant-design-vue/es/message';
import { message } from 'ant-design-vue';

const globalStore = useGlobalStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
let roomName = 'public'

async function init(tryCount: number) {
  if (tryCount > 3) {
    return
  }
  const token = Cookies.get(Constant.JWT_HEADER_NAME)
  if (!token) {
    await autoRegisterAndLogin()
    const { data: res } = await getProfileApi()
    userStore.user = res.data
    await nextTick()
    globalStore.init(roomName)
  } else {
    const ok = await getProfile()
    if (!ok) {
      init(tryCount + 1)
      return
    }
    await nextTick()
    globalStore.init(roomName)
  }
}

async function getProfile() {
  try {
    const { data: res } = await getProfileApi()
    userStore.user = res.data
    return true
  } catch (error) {
    Cookies.remove(Constant.JWT_HEADER_NAME)
    return false
  }
}

declare global {  //设置全局属性
  interface Window {  //window对象属性
    $message: MessageApi   //加入对象
  }
}

window.$message = message

onMounted(async () => {
  await router.isReady()
  roomName = route.query.roomName ? route.query.roomName as string : 'public'
  init(0)
})

</script>

<template>
  <div>
    <RouterView />
  </div>
</template>

<style scoped></style>
