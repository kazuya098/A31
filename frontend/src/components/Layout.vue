<template>
  <div class="flex h-screen bg-[#F7F4EF] font-sans">
    <!-- 左侧极简导航栏 (从Home.vue迁移至此, 统管全局路由) -->
    <nav 
      class="bg-white/70 backdrop-blur-3xl flex flex-col py-8 space-y-12 border-r border-white/40 z-20 shrink-0 shadow-xl transition-all duration-300 ease-in-out"
      :class="isNavExpanded ? 'w-72 items-start pl-4 pr-2 rounded-3xl shadow-2xl shadow-slate-900/10' : 'w-24 items-center rounded-full shadow-lg'"
      @mouseenter="isNavExpanded = true"
      @mouseleave="isNavExpanded = false"
    >
      <!-- 极简 Logo 区 -->
      <div class="logo-area mb-8 w-10 h-10 rounded-xl bg-gradient-to-br from-orange-400 to-orange-600 flex items-center justify-center shadow-lg shadow-orange-500/30"
           :class="isNavExpanded ? 'ml-0' : ''">
        <span v-show="!isNavExpanded" class="text-white font-bold text-xl">B</span>
        <span v-show="isNavExpanded" class="text-white font-bold text-lg whitespace-nowrap overflow-hidden transition-opacity duration-200" :class="{ 'opacity-100': isNavExpanded, 'opacity-0': !isNavExpanded }">BioRec</span>
      </div>
      
      <!-- 导航图标: 使用 Vue Router link 进行切换 -->
      <router-link v-for="(item, index) in navItems" :key="index" :to="item.path"
         class="group relative flex items-center h-14 rounded-xl transition-all duration-300 hover:bg-orange-50 hover:scale-110"
         :class="isNavExpanded ? 'justify-start pl-4 pr-2 w-56' : 'justify-center w-14'"
         active-class="is-active">
        <el-icon :size="26" class="text-slate-400 transition-colors group-hover:text-orange-500" :class="{ 'text-orange-500': $route.path === item.path }">
          <component :is="item.icon" />
        </el-icon>
        <span v-show="isNavExpanded" class="ml-4 text-slate-600 text-lg font-medium whitespace-nowrap overflow-hidden transition-opacity duration-200" :class="{ 'opacity-100': isNavExpanded, 'opacity-0': !isNavExpanded }">{{ item.label }}</span>
        
        <!-- Hover 微光 (Glow) 效果 -->
        <div class="absolute inset-0 rounded-xl bg-orange-400 opacity-0 group-hover:opacity-10 blur-md transition-opacity duration-300" :class="{ 'opacity-10': $route.path === item.path }"></div>
        
        <!-- Active 极细呼吸线 -->
        <div v-show="$route.path === item.path" class="absolute -left-3 top-1/2 -translate-y-1/2 w-1 h-6 bg-orange-500 rounded-r-full shadow-[0_0_10px_rgba(249,115,22,0.8)]"></div>
      </router-link>
      
      <div class="flex-grow"></div>
      
      <!-- 底部设置图标 -->
      <a href="#" @click.prevent="router.push('/profile')"
         class="group relative flex items-center h-14 rounded-xl transition-all duration-300 hover:bg-orange-50 hover:scale-110"
         :class="[isNavExpanded ? 'justify-start pl-4 pr-2 w-56' : 'justify-center w-14', { 'bg-orange-50': $route.path === '/profile' }]"
         :title="profileNavItem.label">
        <el-icon :size="26" class="text-slate-400 transition-colors group-hover:text-orange-500" :class="{ 'text-orange-500': $route.path === '/profile' }">
          <Setting />
        </el-icon>
        <span v-show="isNavExpanded" class="ml-4 text-slate-600 text-lg font-medium whitespace-nowrap overflow-hidden transition-opacity duration-200" :class="{ 'opacity-100': isNavExpanded, 'opacity-0': !isNavExpanded }">设置</span>
        <div class="absolute inset-0 rounded-xl bg-orange-400 opacity-0 group-hover:opacity-10 blur-md transition-opacity duration-300" :class="{ 'opacity-10': $route.path === '/profile' }"></div>
        <div v-show="$route.path === '/profile'" class="absolute -left-3 top-1/2 -translate-y-1/2 w-1 h-6 bg-orange-500 rounded-r-full shadow-[0_0_10px_rgba(249,115,22,0.8)]"></div>
      </a>
      <!-- 退出登录 -->
      <a href="#" @click.prevent="logout"
         class="group relative flex items-center h-14 rounded-xl transition-all duration-300 hover:bg-red-50 hover:scale-110"
         :class="isNavExpanded ? 'justify-start pl-4 pr-2 w-56' : 'justify-center w-14'"
         :title="logoutNavItem.label">
        <el-icon :size="26" class="text-slate-400 transition-colors group-hover:text-red-500"><component :is="logoutNavItem.icon" /></el-icon>
        <span v-show="isNavExpanded" class="ml-4 text-slate-600 text-lg font-medium whitespace-nowrap overflow-hidden transition-opacity duration-200" :class="{ 'opacity-100': isNavExpanded, 'opacity-0': !isNavExpanded }">{{ logoutNavItem.label }}</span>
      </a>
    </nav>

    <!-- 主面板内容区 -->
    <main class="flex-1 overflow-x-hidden flex flex-col p-8">
      <div class="flex-1 bg-[#FDF9F3] backdrop-blur-xl p-10 rounded-[2.5rem] shadow-2xl overflow-auto">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { shallowRef, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Monitor,
  Picture,
  List,
  User,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

const isNavExpanded = ref(false);

// 全局左侧导航栏配置
const navItems = shallowRef([
  { icon: Monitor, path: '/dashboard', label: '控制台' },
  { icon: Picture, path: '/upload', label: '图像识别' },
  { icon: List, path: '/records', label: '识别记录' },
  { icon: User, path: '/individuals', label: '个体档案' },
]);

const profileNavItem = {
  icon: Setting,
  path: '/profile',
  label: '设置',
};

const logoutNavItem = {
  icon: SwitchButton,
  label: '退出登录',
};

// 登出逻辑
const logout = () => {
  localStorage.removeItem('token');
  ElMessage.success('退出登录成功');
  router.push('/login');
};
</script>

<style scoped>
/* 此组件不再需要保留庞杂的旧 CSS，全部依托 Tailwind */
</style>
