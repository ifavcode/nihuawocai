<script setup lang="ts">
import { RouterView } from 'vue-router';
import type { MessageApi } from 'ant-design-vue/es/message';
import { message } from 'ant-design-vue';

declare global {  //设置全局属性
  interface Window {  //window对象属性
    $message: MessageApi   //加入对象
  }
}

window.$message = message

</script>

<template>
  <div>
    <RouterView />
  </div>
</template>

<style scoped></style>
