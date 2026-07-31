<script setup lang="ts">
import { wxAuthorize } from '@/api/user';


async function wxAuthorizeFunc() {
  wxAuthorize().then(res => {
    if (res.data) {
      window.location.href = res.data
    }
  });
}

wxAuthorizeFunc()

</script>

<template>
  <div>
  </div>
</template>