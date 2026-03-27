<template>
  <div class="h-full font-sans">
    <!-- 主面板 -->
    <main class="h-full flex flex-col pt-4 px-8">
      <!-- 头部欢迎区 -->
      <header class="flex justify-between items-center mb-16 animate-fade-in-down">
        <div>
          <h1 class="text-4xl font-light tracking-tight text-gray-900 mb-2">
            欢迎使用 <span class="font-semibold text-orange-600">跨时域生物识别系统</span>
          </h1>
          <p class="text-gray-500 text-lg">安全、高效的跨时域智能识别平台</p>
        </div>
        <button @click="router.push('/upload')" ref="startButton" class="relative isolate overflow-hidden group px-10 py-4 mr-4 rounded-full text-white text-lg font-medium transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2">
          <span class="absolute inset-0 bg-gradient-to-r from-orange-500 to-red-500 transition-transform duration-300 ease-out group-hover:scale-105"></span>
          <!-- Glow element -->
          <span class="absolute -inset-px rounded-full bg-gradient-to-r from-orange-400 via-pink-500 to-purple-600 opacity-0 transition-opacity duration-500 group-hover:opacity-100 animate-spin-slow"
                :style="glowStyle"></span>

          <span class="relative z-10 flex items-center justify-center">
            <span>开始识别</span>
            <span class="ml-2 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
              <el-icon><ArrowRight /></el-icon>
            </span>
            <span class="absolute bottom-0 left-0 w-full h-0.5 bg-transparent transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300 ease-out origin-left"></span>
          </span>
        </button>
      </header>

      <!-- KPI 统计卡片区 -->
      <div class="grid grid-cols-4 gap-12 mb-20">
        <div v-for="(kpi, index) in kpis" :key="index" 
             :class="['p-8 rounded-[2rem] animate-staggered-entry transition-transform hover:-translate-y-2', kpi.bgClass]"
             :style="{ animationDelay: `${index * 100}ms` }">
          <div :class="['w-12 h-12 rounded-full flex items-center justify-center mb-6', kpi.iconBg]">
            <el-icon :class="['w-6 h-6', kpi.iconColor]" :size="24"><component :is="kpi.icon" /></el-icon>
          </div>
          <p class="text-sm text-gray-600 mb-2 font-medium">{{ kpi.title }}</p>
          <p class="text-5xl font-extrabold tracking-tight text-gray-800 mb-4">{{ kpi.value }}</p>
          <p :class="['text-sm font-medium flex items-center', kpi.trendClass]">
            <el-icon class="mr-1"><component :is="kpi.trendIcon" /></el-icon>
            {{ kpi.trend }} <span class="text-gray-500 ml-2 font-normal">本周</span>
          </p>
        </div>
      </div>

      <!-- 图表控制面板区 -->
      <div class="grid grid-cols-3 gap-16 flex-1 mb-16">
        <!-- 柱状图：识别数量统计 -->
        <div class="col-span-2 bg-white/60 backdrop-blur-md border border-white/50 p-10 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up hover:shadow-soft-blue transition-shadow duration-500">
          <div class="flex justify-between items-center mb-8">
            <h3 class="text-xl font-semibold text-gray-800">识别数量走势</h3>
            <div class="bg-gray-100 rounded-full px-4 py-1 text-xs text-gray-500 font-medium">最近 7 天</div>
          </div>
          <BarChart :barData="barData" :lineData="lineData" :categories="categories" height="350px" />
        </div>
        
        <!-- 环状图：准确率统计 -->
        <div class="bg-white/60 backdrop-blur-md border border-white/50 p-10 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up hover:shadow-soft-blue transition-shadow duration-500" style="animation-delay: 300ms;">
          <h3 class="text-xl font-semibold text-gray-800 mb-8">类型分布与准确率</h3>
          <AccuracyPolarChart :data="accuracyData" height="350px" />
          <div class="mt-8 pt-4 border-t border-gray-100 flex justify-around items-center text-sm text-gray-600">
            <div v-for="item in accuracyData" :key="item.name" class="flex items-center space-x-2">
              <span class="w-3 h-3 rounded-full" :style="{ backgroundColor: item.colorConfig[1] }"></span>
              <span>{{ item.name }} ({{ item.value }}%)</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 最近识别记录 -->
      <div class="mt-16 bg-white/60 backdrop-blur-md border border-white/50 p-10 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up" style="animation-delay: 500ms;">
        <div class="flex justify-between items-center mb-8">
          <h3 class="text-xl font-semibold text-gray-800">最近识别记录</h3>
          <button @click="router.push('/records')" class="text-sm font-medium text-orange-600 hover:text-orange-700 hover:underline">查看全部</button>
        </div>
        
        <el-table :data="recentRecords" style="width: 100%" class="custom-table" :show-header="false">
          <el-table-column label="头像" width="70">
            <template #default="scope">
              <el-avatar :src="scope.row.avatar" :size="40" class="shadow-sm"></el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120">
            <template #default="scope">
              <span class="font-medium text-gray-900">{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="ID" width="100">
             <template #default="scope">
              <span class="text-gray-400 font-mono text-xs">{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="识别类型" width="140">
            <template #default="scope">
              <span :class="['px-3 py-1 rounded-full text-xs font-medium', 
                scope.row.type === '人脸识别' ? 'bg-orange-100 text-orange-700' : 'bg-emerald-100 text-emerald-700']">
                {{ scope.row.type }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="识别时间" width="200">
             <template #default="scope">
              <span class="text-gray-500 text-sm">{{ scope.row.time }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="device" label="识别设备">
             <template #default="scope">
              <div class="flex items-center text-gray-600">
                <div class="w-1.5 h-1.5 rounded-full bg-green-500 mr-2"></div>
                {{ scope.row.device }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="right">
            <template #default>
              <button class="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-400 hover:text-gray-900 transition-colors">
                <el-icon><MoreFilled /></el-icon>
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </main>
  </div>
</template>

<script setup>
import { shallowRef, ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import {
  Monitor,
  User,
  Warning,
  TrendCharts,
  ArrowRight,
  CaretTop,
  CaretBottom,
  MoreFilled
} from '@element-plus/icons-vue';
import BarChart from './BarChart.vue';
import AccuracyPolarChart from './AccuracyPolarChart.vue';

const router = useRouter();

const startButton = ref(null);
const glowStyle = ref({});

const handleMouseMove = (e) => {
  if (!startButton.value) return;

  const rect = startButton.value.getBoundingClientRect();
  const x = e.clientX - rect.left; // x position within the element.
  const y = e.clientY - rect.top;  // y position within the element.

  glowStyle.value = {
    background: `radial-gradient(circle at ${x}px ${y}px, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 70%)`
  };
};

const handleMouseLeave = () => {
  glowStyle.value = {}; // Reset glow on mouse leave
};

onMounted(() => {
  if (startButton.value) {
    startButton.value.addEventListener('mousemove', handleMouseMove);
    startButton.value.addEventListener('mouseleave', handleMouseLeave);
  }
});

onUnmounted(() => {
  if (startButton.value) {
    startButton.value.removeEventListener('mousemove', handleMouseMove);
    startButton.value.removeEventListener('mouseleave', handleMouseLeave);
  }
});

// KPI 数据
const kpis = [
  { title: '识别总量', value: '8,432', trend: '+12.5%', trendIcon: CaretTop, icon: Monitor, bgClass: 'bg-white/60 backdrop-blur-md border border-white/50 shadow-soft-blue', iconBg: 'bg-orange-50', iconColor: 'text-orange-500', trendClass: 'text-emerald-600' },
  { title: '活跃用户', value: '1,204', trend: '+5.2%', trendIcon: CaretTop, icon: User, bgClass: 'bg-white/60 backdrop-blur-md border border-white/50 shadow-soft-blue', iconBg: 'bg-blue-50', iconColor: 'text-blue-500', trendClass: 'text-emerald-600' },
  { title: '异常拦截', value: '42', trend: '-18.1%', trendIcon: CaretBottom, icon: Warning, bgClass: 'bg-white/60 backdrop-blur-md border border-white/50 shadow-soft-blue', iconBg: 'bg-red-50', iconColor: 'text-red-500', trendClass: 'text-red-600' },
  { title: '系统准确率', value: '98.7%', trend: '+0.4%', trendIcon: CaretTop, icon: TrendCharts, bgClass: 'bg-gradient-to-br from-orange-500/90 to-orange-500/10 backdrop-blur-md shadow-soft-blue border border-white/50', iconBg: 'bg-orange-50', iconColor: 'text-orange-500', trendClass: 'text-emerald-600' },
];

// 图表数据
const categories = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const barData = [1200, 2000, 1500, 2800, 700, 1100, 1300];
const lineData = [80, 87, 85, 88, 86, 89, 90];

// 极坐标面积图数据
const accuracyData = ref([
  { value: 95, name: '面部特征', colorConfig: ['#fed7aa', '#f97316'] },
  { value: 88, name: '指纹信息', colorConfig: ['#fca5a5', '#ef4444'] },
  { value: 76, name: '步态轨迹', colorConfig: ['#a7f3d0', '#10b981'] },
]);

// 在挂载后初始化渐变色，避免在 setup 阶段 ECharts 未就绪导致的奔溃
onMounted(() => {
  accuracyData.value = accuracyData.value.map(item => ({
    ...item,
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: item.colorConfig[0] },
        { offset: 1, color: item.colorConfig[1] }
      ])
    }
  }));
});

// 最近记录数据
const recentRecords = ref([
  { avatar: 'https://cube.elemecdn.com/0/88/03b0dff330f245542281691515e9c.jpeg', name: 'Alena Smith', id: 'USR-001', type: '人脸识别', time: '10分钟前', device: '入口闸机 Alpha' },
  { avatar: 'https://cube.elemecdn.com/0/88/03b0dff330f245542281691515e9c.jpeg', name: 'John Doe', id: 'USR-082', type: '指纹识别', time: '25分钟前', device: '财务室门禁' },
  { avatar: 'https://cube.elemecdn.com/0/88/03b0dff330f245542281691515e9c.jpeg', name: 'Alice Wang', id: 'USR-124', type: '人脸识别', time: '1小时前', device: '主会议室' },
]);
</script>

<style scoped>


</style>
