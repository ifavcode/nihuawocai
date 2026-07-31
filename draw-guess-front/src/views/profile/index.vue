<script setup lang="ts">
import { changePasswordApi, getDrawProfileApi, getProfileApi, loginApi, registerApi, updateProfileApi, uploadFileApi, wxAuthorize } from '@/api/user'
import { useUserStore } from '@/store/userStore'
import { Constant, type GameRound, type User } from '@/types'
import { autoRegisterAndLogin, emitter, formatDate } from '@/utils'
import { message, Modal } from 'ant-design-vue'
import Cookies from 'js-cookie'
import { Icon } from '@iconify/vue'
import NavBar from '@/views/navbar/index.vue'

const router = useRouter()
const userStore = useUserStore()

// 登录表单
const username = ref('')
const password = ref('')
const loginLoading = ref(false)
const wechatLoginLoading = ref(false)

// 作品列表
const recommendList = ref<GameRound[]>([])
const worksLoading = ref(false)

// 编辑个人信息
const editingNickname = ref(false)
const editNicknameValue = ref('')
const profileUpdating = ref(false)

// 修改密码
const showPwdModal = ref(false)
const pwdForm = reactive({ rawPassword: '', newPassword: '', confirmPassword: '' })
const pwdChanging = ref(false)

const avatarInputRef = ref<HTMLInputElement | null>(null)

// 是否已登录 — 仅依赖响应式数据（userStore.user.id），
// Cookies.get() 不是响应式的，放在 onMounted 中做一次性检查
const isLoggedIn = computed(() => !!userStore.user.id)

// 一键自动注册并登录
async function handleAutoLogin() {
  loginLoading.value = true
  try {
    await autoRegisterAndLogin()
    const { data: res } = await getProfileApi()
    userStore.user = res.data
    message.success('登录成功')
    getDrawRecommend()
  } catch (error) {
    console.warn('自动登录失败:', error)
    message.error('自动登录失败，请重试')
  } finally {
    loginLoading.value = false
  }
}

// 注册并登录（使用输入的用户名密码）
async function handleRegister() {
  if (!username.value.trim()) {
    message.warning('请输入用户名')
    return
  }
  if (!password.value.trim()) {
    message.warning('请输入密码')
    return
  }
  loginLoading.value = true
  try {
    const user: User = {
      username: username.value.trim(),
      password: password.value,
    }
    await registerApi(user)
    await loginApi(user)
    const { data: res } = await getProfileApi()
    userStore.user = res.data
    message.success('注册并登录成功')
    getDrawRecommend()
  } catch (error) {
    console.warn('注册失败:', error)
    message.error('注册失败，用户名可能已被占用')
  } finally {
    loginLoading.value = false
  }
}

// 使用已有账号登录
async function handleLogin() {
  if (!username.value.trim()) {
    message.warning('请输入用户名')
    return
  }
  if (!password.value.trim()) {
    message.warning('请输入密码')
    return
  }
  loginLoading.value = true
  try {
    const user: User = {
      username: username.value.trim(),
      password: password.value,
    }
    await loginApi(user)
    const { data: res } = await getProfileApi()
    userStore.user = res.data
    message.success('登录成功')
    getDrawRecommend()
  } catch (error) {
    console.warn('登录失败:', error)
    message.error('登录失败，请检查用户名和密码')
  } finally {
    loginLoading.value = false
  }
}

// 微信登录
async function handleWechatLogin() {
  wechatLoginLoading.value = true
  try {
    const { data: url } = await wxAuthorize()
    if (url) {
      window.location.href = url
    }
  } catch (error) {
    console.warn('微信登录失败:', error)
    message.error('微信登录失败，请重试')
  } finally {
    wechatLoginLoading.value = false
  }
}

// 获取用户作品
async function getDrawRecommend() {
  worksLoading.value = true
  recommendList.value = []
  try {
    const { data: res } = await getDrawProfileApi()
    if (res.data) {
      recommendList.value = res.data
    }
  } catch (error) {
    console.warn('作品加载失败:', error)
  } finally {
    worksLoading.value = false
  }
}

// 开始编辑昵称
function startEditNickname() {
  editNicknameValue.value = userStore.user.nickname || ''
  editingNickname.value = true
}

// 保存昵称
async function saveNickname() {
  const newNickname = editNicknameValue.value.trim()
  if (!newNickname || newNickname === userStore.user.nickname) {
    editingNickname.value = false
    return
  }
  profileUpdating.value = true
  try {
    await updateProfileApi({ nickname: newNickname, avatar: userStore.user.avatar || '' })
    userStore.user = { ...userStore.user, nickname: newNickname }
    message.success('昵称已更新')
  } catch {
    message.error('更新失败，请重试')
  } finally {
    profileUpdating.value = false
    editingNickname.value = false
  }
}

// 修改密码
function openPwdModal() {
  pwdForm.rawPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  showPwdModal.value = true
}

async function handleChangePassword() {
  if (!pwdForm.rawPassword) {
    message.warning('请输入原密码')
    return
  }
  if (!pwdForm.newPassword) {
    message.warning('请输入新密码')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    message.warning('两次输入的新密码不一致')
    return
  }
  pwdChanging.value = true
  try {
    const { data: res } = await changePasswordApi({ rawPassword: pwdForm.rawPassword, newPassword: pwdForm.newPassword })
    if (res.code === 200) {
      message.success('密码已修改')
      showPwdModal.value = false
    } else {
      message.error(res.msg)
    }
  } catch {
    message.error('修改失败，请检查原密码是否正确')
  } finally {
    pwdChanging.value = false
  }
}

// 选择并上传头像
function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

async function handleAvatarFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  profileUpdating.value = true
  try {
    const { data: uploadRes } = await uploadFileApi(file)
    const avatarUrl = uploadRes.data
    await updateProfileApi({ nickname: userStore.user.nickname || '', avatar: avatarUrl })
    userStore.user = { ...userStore.user, avatar: avatarUrl }
    message.success('头像已更新')
  } catch {
    message.error('头像上传失败，请重试')
  } finally {
    profileUpdating.value = false
    input.value = ''
  }
}

function enterMyRoom() {
  if (userStore.user.username) {
    router.push({
      name: 'room',
      query: { roomName: userStore.user.username },
    })
  } else {
    message.error('进入失败，请重试')
  }
}

function enterPublicRoom() {
  router.push({
    name: 'room',
    query: { roomName: 'public' },
  })
}

function download(url: string) {
  window.open(url)
}

function logout() {
  Modal.confirm({
    title: '确认退出',
    content: '退出后将以新身份重新登录，确定要退出吗？',
    okText: '确定退出',
    cancelText: '取消',
    okType: 'danger',
    centered: true,
    onOk() {
      Cookies.remove(Constant.JWT_HEADER_NAME)
      userStore.user = {}
      recommendList.value = []
    },
  })
}

// 页面加载时如果已登录则获取作品
onMounted(() => {
  emitter.on('loginSuccess', () => {
    getDrawRecommend()
  })
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
              我的
            </span>
          </h1>
        </div>
      </a-affix>

      <!-- ==================== 未登录：登录/注册表单 ==================== -->
      <div v-if="!isLoggedIn" class="mt-6 p-8 bg-white rounded-2xl border border-gray-100 shadow-sm">
        <div class="flex flex-col items-center mb-6">
          <div class="size-16 rounded-full bg-purple-50 flex items-center justify-center mb-3">
            <Icon icon="material-symbols:person" class="text-3xl text-purple-400" />
          </div>
          <h2 class="text-lg font-semibold text-gray-700">登录 / 注册</h2>
          <p class="text-xs text-gray-400 mt-1">登录后可使用完整功能</p>
        </div>

        <!-- 一键自动登录 -->
        <div class="flex flex-col items-center mb-6">
          <RainbowButton
            :class="`text-white text-base px-10 py-3 w-full max-w-xs${loginLoading ? ' pointer-events-none opacity-60' : ''}`"
            @click="handleAutoLogin">
            <Icon icon="material-symbols:auto-awesome" class="text-lg" />
            <span class="mx-2">一键注册 / 登录</span>
          </RainbowButton>
          <p class="text-xs text-gray-300 mt-2">自动创建随机账号，无需手动输入</p>
        </div>

        <!-- 分割线 -->
        <div class="flex items-center gap-3 mb-6">
          <div class="flex-1 border-t border-gray-100"></div>
          <span class="text-xs text-gray-300">或手动输入</span>
          <div class="flex-1 border-t border-gray-100"></div>
        </div>

        <!-- 手动输入表单 -->
        <div class="flex flex-col gap-3">
          <div class="relative">
            <Icon icon="material-symbols:person-outline"
              class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-lg" />
            <input v-model="username" type="text" placeholder="请输入用户名"
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm text-gray-700 placeholder:text-gray-300 outline-none transition-all duration-200 focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
              @keyup.enter="handleLogin" />
          </div>
          <div class="relative">
            <Icon icon="material-symbols:lock-outline"
              class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-lg" />
            <input v-model="password" type="password" placeholder="请输入密码"
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm text-gray-700 placeholder:text-gray-300 outline-none transition-all duration-200 focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
              @keyup.enter="handleLogin" />
          </div>

          <div class="flex gap-3 mt-1">
            <button
              class="flex-1 py-2.5 rounded-xl border-2 border-purple-200 text-purple-600 font-medium text-sm bg-white transition-all duration-200 hover:border-purple-400 hover:bg-purple-50 active:scale-95 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="loginLoading" @click="handleRegister">
              注册并登录
            </button>
            <button
              class="flex-1 py-2.5 rounded-xl bg-purple-600 text-white font-medium text-sm transition-all duration-200 hover:bg-purple-700 active:scale-95 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="loginLoading" @click="handleLogin">
              登录
            </button>
          </div>

          <!-- 微信登录按钮 -->
          <button
            class="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl bg-green-500 text-white font-medium text-sm transition-all duration-200 hover:bg-green-600 active:scale-95 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="wechatLoginLoading" @click="handleWechatLogin">
            <Icon v-if="!wechatLoginLoading" icon="ri:wechat-fill" class="text-lg" />
            <span v-else class="svg-spinners--ring-resize"></span>
            微信登录
          </button>
        </div>
      </div>

      <!-- ==================== 已登录：个人中心 ==================== -->
      <template v-else>
        <!-- 用户信息卡片 -->
        <div class="mt-6 p-8 bg-white rounded-2xl border border-gray-100 shadow-sm">
          <div class="flex flex-col items-center">
            <!-- 头像：点击上传 -->
            <div class="relative cursor-pointer group" @click="triggerAvatarUpload"
              :class="{ 'pointer-events-none': profileUpdating }">
              <a-avatar :src="userStore.user.avatar" :size="80"
                class="border-4 border-purple-100 shadow-md transition-opacity group-hover:opacity-70">
                <template #icon>
                  <Icon icon="material-symbols:person" class="text-4xl text-gray-300" />
                </template>
              </a-avatar>
              <div
                class="absolute inset-0 flex items-center justify-center rounded-full bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity">
                <Icon v-if="!profileUpdating" icon="material-symbols:photo-camera" class="text-2xl text-white" />
                <p v-else class="svg-spinners--ring-resize text-white text-xl"></p>
              </div>
            </div>
            <input ref="avatarInputRef" type="file" accept="image/*" class="hidden" @change="handleAvatarFileChange" />

            <!-- 昵称 -->
            <div class="mt-4 flex items-center gap-2">
              <template v-if="editingNickname">
                <input v-model="editNicknameValue" type="text" maxlength="20"
                  class="w-40 px-3 py-1.5 rounded-lg border border-purple-300 text-lg font-semibold text-gray-800 text-center outline-none focus:ring-2 focus:ring-purple-200"
                  @keyup.enter="saveNickname" @keyup.escape="editingNickname = false" @blur="saveNickname" />
              </template>
              <template v-else>
                <h2 class="text-xl font-semibold text-gray-800 cursor-pointer hover:text-purple-500 transition-colors"
                  @click="startEditNickname">
                  {{ userStore.user.nickname || '未设置昵称' }}
                  <Icon icon="material-symbols:edit-outline"
                    class="inline-block ml-1 text-sm text-gray-300 align-middle" />
                </h2>
              </template>
            </div>

            <p class="text-sm text-gray-400 mt-0.5">
              @{{ userStore.user.username || '...' }}
            </p>
            <p v-if="userStore.user.createTime" class="text-xs text-gray-300 mt-2 flex items-center gap-1">
              <Icon icon="material-symbols:calendar-month-outline" class="text-sm" />
              加入于 {{ formatDate(userStore.user.createTime) }}
            </p>
          </div>
        </div>

        <!-- 快捷入口 -->
        <div class="flex flex-col items-center justify-center gap-3 mt-6">
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

        <!-- 分隔线 -->
        <div class="border-t border-gray-100 my-6"></div>

        <!-- 我的作品 -->
        <div>
          <h2 class="text-lg font-semibold flex items-center gap-2 mb-3 text-gray-700">
            <Icon icon="material-symbols:draw" class="text-xl text-gray-400" />
            我的作品
            <button
              class="p-1 rounded-lg text-gray-400 hover:text-purple-500 hover:bg-purple-50 transition-colors duration-200 cursor-pointer"
              :class="{ 'animate-spin text-purple-500': worksLoading }" :disabled="worksLoading"
              @click="getDrawRecommend">
              <Icon icon="material-symbols:refresh" class="text-lg" />
            </button>
          </h2>

          <!-- 加载中 -->
          <div v-if="worksLoading" class="flex flex-col items-center justify-center py-12 text-gray-400">
            <p class="svg-spinners--ring-resize text-purple-400 text-3xl"></p>
            <p class="text-sm text-gray-400 mt-3">加载中...</p>
          </div>

          <!-- 空状态 -->
          <div v-else-if="!recommendList || recommendList.length === 0"
            class="flex flex-col items-center justify-center py-12 text-gray-400 bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
            <Icon icon="material-symbols:imagesmode-outline" class="text-5xl mb-3 text-gray-300" />
            <p class="text-sm">还没有作品</p>
            <p class="text-xs text-gray-300 mt-1">完成画作后将会出现在这里</p>
          </div>

          <div v-else class="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <div v-for="item in recommendList" :key="item.imageUrl"
              class="aspect-square rounded-xl overflow-hidden bg-white border border-gray-100 shadow-sm cursor-pointer transition-all duration-300 hover:scale-[1.03] hover:shadow-lg hover:border-gray-200"
              @click="download(item.imageUrl)">
              <img class="w-full h-full object-cover" :src="item.imageUrl" alt="作品" loading="lazy">
            </div>
          </div>
        </div>

        <!-- 修改密码 & 退出登录 -->
        <div class="mt-8 flex justify-center gap-6">
          <button
            class="flex items-center gap-1.5 px-4 py-2 text-sm text-gray-400 hover:text-purple-500 transition-colors duration-200 cursor-pointer"
            @click="openPwdModal">
            <Icon icon="material-symbols:lock-outline" class="text-lg" />
            修改密码
          </button>
          <button
            class="flex items-center gap-1.5 px-4 py-2 text-sm text-gray-400 hover:text-red-500 transition-colors duration-200 cursor-pointer"
            @click="logout">
            <Icon icon="material-symbols:logout" class="text-lg" />
            退出登录
          </button>
        </div>

        <!-- 修改密码弹窗 -->
        <a-modal v-model:open="showPwdModal" title="修改密码" centered :footer="null" :destroy-on-close="true"
          width="360px">
          <div class="flex flex-col gap-3 pt-2">
            <div class="flex items-center gap-3">
              <label class="text-sm text-gray-600 w-16 shrink-0">原密码</label>
              <div class="relative flex-1">
                <input v-model="pwdForm.rawPassword" type="password" placeholder="请输入原密码"
                  class="w-full pl-3 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-100" />
              </div>
            </div>
            <div class="flex items-center gap-3">
              <label class="text-sm text-gray-600 w-16 shrink-0">新密码</label>
              <div class="relative flex-1">
                <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码"
                  class="w-full pl-3 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-100" />
              </div>
            </div>
            <div class="flex items-center gap-3">
              <label class="text-sm text-gray-600 w-16 shrink-0">确认密码</label>
              <div class="relative flex-1">
                <input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码"
                  class="w-full pl-3 pr-4 py-2.5 rounded-xl border border-gray-200 text-sm outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
                  @keyup.enter="handleChangePassword" />
              </div>
            </div>
            <button
              class="mt-2 w-full py-2.5 rounded-xl bg-purple-600 text-white font-medium text-sm transition-all duration-200 hover:bg-purple-700 active:scale-95 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="pwdChanging" @click="handleChangePassword">
              <span v-if="!pwdChanging">确认修改</span>
              <span v-else class="inline-flex items-center gap-1">
                <span class="svg-spinners--ring-resize text-white"></span>修改中...
              </span>
            </button>
          </div>
        </a-modal>
      </template>

      <!-- 底部留白（为固定导航条留空间） -->
      <div class="h-16"></div>
    </div>

    <NavBar />
  </div>
</template>

<style scoped></style>
