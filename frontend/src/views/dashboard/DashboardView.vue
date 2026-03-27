<template>
  <div class="dashboard-view-container">
    <Home />
  </div>
</template>

<script setup>
import Home from '@/components/Home.vue';
</script>

<style scoped>
.dashboard-view-container {
  /* 可以放置 DashboardView 自己的样式，如果需要的话 */
  height: 100%; /* 确保Home组件可以占据所有可用空间 */
  width: 100%;
}
</style>
