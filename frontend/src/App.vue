<template>
  <el-config-provider :message="messageConfig">
    <div class="app-root">
      <router-view />
    </div>
  </el-config-provider>
</template>

<script setup>
const messageConfig = {
  max: 3,
  grouping: true,
  duration: 3000,
  offset: 76
}
</script>