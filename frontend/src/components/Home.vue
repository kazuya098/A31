<template>
  <div class="h-full font-sans overflow-y-auto">
    <!-- 主面板: 移除顶部内边距，让图片全覆盖 -->
    <main class="min-h-full flex flex-col bg-[#FDF9F3]">
      
      <!-- 1. 顶部全覆盖背景图 Banner -->
      <div class="relative w-full h-[280px] shrink-0 overflow-hidden shadow-lg rounded-[3rem]">
        <img src="@/assets/images/dashboard/dashboard.jpg" class="absolute inset-0 w-full h-full object-cover" alt="Dashboard Banner">
        <!-- 强化版橙色滤镜 (更为显著的品牌色渗透) -->
        <div class="absolute inset-0 bg-gradient-to-br from-orange-600/85 via-orange-500/40 to-transparent mix-blend-multiply"></div>
        <div class="absolute inset-0 bg-gradient-to-tr from-orange-400/20 to-transparent"></div>
        <!-- 文字保护蒙层: 稍微收窄，让橙色更外溢 -->
        <div class="absolute inset-0 bg-gradient-to-r from-[#FDF9F3]/90 via-[#FDF9F3]/10 to-transparent flex flex-col justify-center px-16">
          <header class="animate-fade-in-down">
            <div>
              <h1 class="text-4xl font-light tracking-tight text-gray-900 mb-2">
                欢迎使用 <span class="font-black text-orange-600 tracking-tighter">跨时域生物识别系统</span>
              </h1>
              <p class="text-gray-500 text-lg font-medium max-w-xl leading-relaxed">
                基于深度时序神经网络，为您提供安全、高效、精准的识别追踪平台。
              </p>
            </div>
            
            <div class="mt-6 flex items-center gap-6">
              <button @click="goToUpload" ref="startButton" class="relative isolate overflow-hidden group px-10 py-3.5 rounded-xl text-white text-lg font-bold transition-all duration-300 ease-out shadow-lg shadow-orange-500/20">
                <span class="absolute inset-0 bg-gradient-to-r from-orange-500 to-red-600 transition-transform duration-300 ease-out group-hover:scale-105"></span>
                <span class="relative z-10 flex items-center justify-center">
                  <span>开始识别任务</span>
                  <span class="ml-3 transform transition-transform duration-300 ease-out group-hover:translate-x-1">
                    <el-icon><ArrowRight /></el-icon>
                  </span>
                </span>
              </button>
              
              <div class="flex items-center gap-2 text-gray-400">
                <span :class="['w-1.5 h-1.5 rounded-full', systemStatus === 'up' ? 'bg-emerald-500 animate-ping' : 'bg-red-500']"></span>
                <span class="text-sm font-bold tracking-widest uppercase italic">
                  {{ systemStatus === 'up' ? 'System Real-time Monitoring' : 'Service Connection Lost' }}
                </span>
              </div>
            </div>
          </header>
        </div>
      </div>

      <!-- 2. 下方内容区 -->
      <div class="px-12 py-10 space-y-12">
        
        <!-- KPI 统计卡片区 -->
        <div class="grid grid-cols-4 gap-8">
          <div v-for="(kpi, index) in kpis" :key="index" 
               :class="['p-8 rounded-[2rem] shadow-sm flex flex-col justify-between transition-all duration-300 hover:shadow-xl hover:-translate-y-1', kpi.bgClass]">
            <div>
              <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center mb-6 shadow-sm', kpi.iconBg]">
                <el-icon :class="['w-6 h-6', kpi.iconColor]" :size="24"><component :is="kpi.icon" /></el-icon>
              </div>
              <p class="text-xs text-gray-500 mb-1 font-black uppercase tracking-widest">{{ kpi.title }}</p>
              <p class="text-4xl font-black tracking-tight text-gray-900 mb-2">{{ kpi.value }}</p>
            </div>
            <p :class="['text-[11px] font-bold flex items-center mt-4 px-3 py-1 rounded-full w-fit bg-white/50 backdrop-blur-sm', kpi.trendClass]">
              <el-icon class="mr-1"><component :is="kpi.trendIcon" /></el-icon>
              {{ kpi.trend }} <span class="text-gray-400 ml-1 font-normal uppercase">vs last week</span>
            </p>
          </div>
        </div>

        <!-- 图表控制面板区 -->
        <div class="grid grid-cols-3 gap-12">
          <!-- 柱状图：识别数量统计 -->
          <div class="col-span-2 bg-white/60 backdrop-blur-md border border-white/40 p-10 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up transition-shadow duration-500">
            <div class="flex justify-between items-center mb-8">
              <h3 class="text-xl font-bold text-gray-900 tracking-tight">识别数量走势</h3>
              <div class="bg-gray-100 rounded-full px-4 py-1 text-[10px] text-gray-500 font-bold uppercase tracking-wider">Last 7 Days</div>
            </div>
            <BarChart :barData="barData" :lineData="lineData" :categories="categories" height="350px" />
          </div>
          
          <!-- 环状图：准确率统计 -->
          <div class="bg-white/60 backdrop-blur-md border border-white/40 p-10 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up shadow-sm transition-shadow duration-500">
            <h3 class="text-xl font-bold text-gray-900 tracking-tight mb-8">类型分布与准确率</h3>
            <AccuracyPolarChart :data="accuracyData" height="350px" />
            <div class="mt-8 pt-4 border-t border-gray-200/40 flex justify-around items-center text-[11px] font-bold text-gray-500 uppercase">
              <div v-for="item in accuracyData" :key="item.name" class="flex items-center space-x-2">
                <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: item.colorConfig[1] }"></span>
                <span>{{ item.name }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 最近识别记录 -->
        <div class="bg-white/60 backdrop-blur-md border border-white/40 p-10 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up mb-10">
          <div class="flex justify-between items-center mb-10">
            <h3 class="text-2xl font-black text-gray-900 tracking-tight">最近识别记录</h3>
            <button @click="goToRecords" class="text-sm font-bold text-orange-600 hover:text-orange-700 underline underline-offset-4 decoration-2">查看全部记录</button>
          </div>
          
          <el-table :data="recentRecords" style="width: 100%" class="custom-table" :show-header="false">
            <el-table-column label="头像" width="80">
              <template #default="scope">
                <el-avatar :src="scope.row.avatar" :size="48" class="shadow-sm border-2 border-white"></el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="姓名" min-width="150">
              <template #default="scope">
                <span class="text-base font-bold text-gray-900">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="ID" width="120">
               <template #default="scope">
                <span class="text-gray-400 font-mono text-xs italic">#{{ scope.row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="识别类型" width="160">
              <template #default="scope">
                <span :class="['px-4 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-wider', 
                  scope.row.type === '人脸识别' ? 'bg-orange-500 text-white' : 'bg-emerald-500 text-white shadow-sm']">
                  {{ scope.row.type }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="识别时间" width="220">
               <template #default="scope">
                <div class="flex flex-col text-sm">
                  <span class="font-bold text-gray-700">{{ scope.row.time }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="device" label="识别设备" min-width="120">
               <template #default="scope">
                <div class="flex items-center text-gray-500 font-medium">
                  <div class="w-2 h-2 rounded-full bg-emerald-400 mr-3 animate-pulse"></div>
                  {{ scope.row.device }}
                </div>
              </template>
            </el-table-column>
            <el-table-column width="120" fixed="right" align="right">
              <template #default>
                <el-button type="primary" link @click="goToUpload" class="font-black text-xs uppercase tracking-widest hover:translate-x-1 transition-transform">
                  详情 <el-icon class="ml-1"><ArrowRight /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { shallowRef, ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getRecognitionRecords } from '@/services/modules/recognition.js';
import { getHealth } from '@/services/modules/system.js';
import * as echarts from 'echarts';
import {
  Monitor,
  User,
  Warning,
  TrendCharts,
  ArrowRight,
  CaretTop,
  CaretBottom,
  MoreFilled,
  List
} from '@element-plus/icons-vue';
import BarChart from './BarChart.vue';
import AccuracyPolarChart from './AccuracyPolarChart.vue';

const router = useRouter();
const goToUpload = () => router.push('/upload');
const goToRecords = () => router.push('/records');

const systemStatus = ref('up');

const checkHealth = async () => {
  try {
    const res = await getHealth();
    const data = res?.data || res;
    // 兼容后端返回结构 { code, data: { status } } 或直接返回 { status }
    if (data.status === 'up' || data.data?.status === 'up') {
      systemStatus.value = 'up';
    } else {
      systemStatus.value = 'down';
    }
  } catch (error) {
    systemStatus.value = 'down';
    console.error('System health check failed:', error);
  }
};

const startButton = ref(null);
const glowStyle = ref({});

const handleMouseMove = (e) => {
  if (!startButton.value) return;
  const rect = startButton.value.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  glowStyle.value = {
    background: `radial-gradient(circle at ${x}px ${y}px, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 70%)`
  };
};

const handleMouseLeave = () => {
  glowStyle.value = {};
};

const fetchRecentRecords = async () => {
  try {
    const res = await getRecognitionRecords();
    const data = res?.data || res;
    if (data && Array.isArray(data)) {
      recentRecords.value = data.slice(0, 3).map(record => ({
        avatar: toAbsoluteUrl(record.imagePath) || 'https://cube.elemecdn.com/0/88/03b0dff330f245542281691515e9c.jpeg',
        name: record.recognitionResult || '未知个体',
        id: record.id,
        type: record.type === 'human' ? '人脸识别' : '非人识别',
        time: formatRelativeTime(record.recognitionTime),
        device: record.operationStatus || '边缘计算节点 Alpha',
        rawId: record.id
      }));

      const total = data.length;
      kpis.value[0].value = total.toLocaleString();
      const users = new Set(data.map(r => r.operatorName).filter(Boolean));
      kpis.value[1].value = users.size.toLocaleString();
      const anomalies = data.filter(r => r.confidence < 0.7).length;
      kpis.value[2].value = anomalies.toLocaleString();
      const avgConfidence = data.reduce((acc, r) => acc + (r.confidence || 0), 0) / (total || 1);
      kpis.value[3].value = (avgConfidence * 100).toFixed(1) + '%';
    }
  } catch (error) {
    console.error('Failed to fetch recent records:', error);
  }
};

const toAbsoluteUrl = (maybePath) => {
  if (!maybePath) return null;
  if (/^https?:\/\//i.test(maybePath)) return maybePath;
  
  // 拼接代理路径
  const base = import.meta.env.VITE_API_BASE_URL || '/api';
  const path = maybePath.startsWith('/') ? maybePath : '/' + maybePath;
  return base + path;
};

const formatRelativeTime = (isoStr) => {
  if (!isoStr) return '-';
  const now = new Date();
  const date = new Date(isoStr);
  const diff = now - date;
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return '刚刚';
};

const kpis = ref([
  { title: '识别总量', value: '8,432', trend: '+12.5%', trendIcon: CaretTop, icon: Monitor, bgClass: 'bg-white/60 backdrop-blur-md border border-white/50 shadow-soft-blue', iconBg: 'bg-orange-50', iconColor: 'text-orange-500', trendClass: 'text-emerald-600' },
  { title: '活跃用户', value: '1,204', trend: '+5.2%', trendIcon: CaretTop, icon: User, bgClass: 'bg-white/60 backdrop-blur-md border border-white/50 shadow-soft-blue', iconBg: 'bg-blue-50', iconColor: 'text-blue-500', trendClass: 'text-emerald-600' },
  { title: '异常拦截', value: '42', trend: '-18.1%', trendIcon: CaretBottom, icon: Warning, bgClass: 'bg-white/60 backdrop-blur-md border border-white/50 shadow-soft-blue', iconBg: 'bg-red-50', iconColor: 'text-red-500', trendClass: 'text-red-600' },
  { title: '系统准确率', value: '98.7%', trend: '+0.4%', trendIcon: CaretTop, icon: TrendCharts, bgClass: 'bg-gradient-to-br from-orange-500/90 to-orange-500/10 backdrop-blur-md shadow-soft-blue border border-white/50', iconBg: 'bg-orange-50', iconColor: 'text-orange-500', trendClass: 'text-emerald-600' },
]);

const categories = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const barData = [1200, 2000, 1500, 2800, 700, 1100, 1300];
const lineData = [80, 87, 85, 88, 86, 89, 90];

const accuracyData = ref([
  { value: 95, name: '面部特征', colorConfig: ['#fed7aa', '#f97316'] },
  { value: 88, name: '指纹信息', colorConfig: ['#fca5a5', '#ef4444'] },
  { value: 76, name: '步态轨迹', colorConfig: ['#a7f3d0', '#10b981'] },
]);

onMounted(() => {
  fetchRecentRecords();
  checkHealth();
  if (startButton.value) {
    startButton.value.addEventListener('mousemove', handleMouseMove);
    startButton.value.addEventListener('mouseleave', handleMouseLeave);
  }
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

onUnmounted(() => {
  if (startButton.value) {
    startButton.value.removeEventListener('mousemove', handleMouseMove);
    startButton.value.removeEventListener('mouseleave', handleMouseLeave);
  }
});

const recentRecords = ref([]);
</script>

<style scoped>
.shadow-soft-blue {
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
}
.custom-table :deep(.el-table__row) {
  background-color: transparent !important;
}
</style>
