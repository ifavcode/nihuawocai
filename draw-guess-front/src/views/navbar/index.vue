<script setup lang="ts">
import { Icon } from '@iconify/vue'
import { useRoute, useRouter } from 'vue-router'

interface NavItem {
  name: string
  label: string
  icon: string
  activeIcon: string
  routeName: string
}

const route = useRoute()
const router = useRouter()

const navItems: NavItem[] = [
  {
    name: 'hall',
    label: '大厅',
    icon: 'material-symbols:home-outline',
    activeIcon: 'material-symbols:home',
    routeName: 'hall',
  },
  {
    name: 'profile',
    label: '我的',
    icon: 'material-symbols:person-outline',
    activeIcon: 'material-symbols:person',
    routeName: 'profile',
  },
]

function isActive(item: NavItem): boolean {
  if (item.routeName === 'hall') {
    return route.name === 'hall'
  }
  return route.name === item.routeName
}

function navigate(item: NavItem) {
  router.push({ name: item.name })
}
</script>

<template>
  <nav class="fixed bottom-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-lg border-t border-gray-100">
    <div class="max-w-[680px] mx-auto flex items-center justify-around h-14 px-4">
      <button v-for="item in navItems" :key="item.name"
        class="flex flex-col items-center justify-center gap-0.5 min-w-[64px] h-full transition-colors duration-200 cursor-pointer"
        :class="isActive(item) ? 'text-purple-600' : 'text-gray-400 hover:text-gray-600'" @click="navigate(item)">
        <Icon :icon="isActive(item) ? item.activeIcon : item.icon" class="text-xl transition-transform duration-200"
          :class="{ 'scale-110': isActive(item) }" />
        <span class="text-xs font-medium">{{ item.label }}</span>
      </button>
    </div>
  </nav>
</template>

<style scoped></style>
