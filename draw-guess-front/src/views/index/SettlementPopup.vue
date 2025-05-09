<script setup lang="ts">
withDefaults(defineProps<{
  imgUrl: string;
  title: string;
}>(), {
  title: '',
  imgUrl: '',
})

const isShow = ref(true)


function throwGif(){

}

defineExpose({ isShow })
</script>

<template>
  <a-modal v-model:open="isShow" :footer="null" title="回合结束">
    <h1 class="text-base font-semibold">标题：
      <span class="text-primary">{{ title }}</span>
    </h1>
    <img :src="imgUrl" alt="">
    <div class="flex gap-2">
      <a-button class="w-14" @click="throwGif">🥚</a-button>
      <a-button class="w-14">❀</a-button>
    </div>
  </a-modal>
</template>