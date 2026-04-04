<template>
  <div class="h-full font-sans bg-[#F7F4EF] flex flex-col overflow-hidden">
    <!-- Header -->
    <header class="flex justify-between items-center mb-16 py-6 px-10 shrink-0 animate-fade-in-down">
      <div>
        <h1 class="text-4xl font-light tracking-tight text-gray-900 mb-2">
          个体档案 <span class="font-semibold text-orange-600">管理中心</span>
        </h1>
        <p class="text-gray-500 text-lg">查看同物种个体的详细历史记录与识别报告</p>
      </div>
      <div class="flex items-center space-x-4">
        <el-input
          v-model="searchQuery"
          placeholder="搜索个体 ID..."
          class="custom-search-input w-64"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" circle :icon="Refresh" @click="fetchIndividuals" />
      </div>
    </header>

    <!-- Main Content Area -->
    <div class="flex-1 flex px-10 pb-10 gap-8 overflow-hidden">
      <!-- Sidebar: Individual List -->
      <aside class="w-1/3 flex flex-col gap-6 h-full overflow-hidden">
        <div class="bg-white/60 backdrop-blur-md border border-white/50 rounded-[2rem] shadow-soft-blue flex flex-col h-full overflow-hidden transition-all duration-500">
          <div class="p-6 border-b border-gray-100/50 flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-800">个体列表</h3>
            <span class="px-3 py-1 bg-orange-100 text-orange-600 rounded-full text-xs font-medium">
              共 {{ filteredIndividuals.length }} 个
            </span>
          </div>
          
          <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
            <div v-if="loading" class="flex flex-col items-center justify-center h-full space-y-4">
              <el-icon class="animate-spin text-orange-400" size="32"><Loading /></el-icon>
              <p class="text-gray-400 text-sm">正在加载数据...</p>
            </div>
            
            <div v-else-if="filteredIndividuals.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400">
               <el-icon size="48" class="mb-2 opacity-20"><UserFilled /></el-icon>
               <p>暂无符合条件的个体</p>
            </div>

            <div v-else class="space-y-4">
              <div 
                v-for="item in filteredIndividuals" 
                :key="item.id"
                @click="selectIndividual(item.id)"
                :class="[
                  'p-4 rounded-2xl border transition-all duration-300 cursor-pointer group hover:shadow-md',
                  selectedIndividualId === item.id 
                    ? 'bg-orange-50/80 border-orange-200/50 shadow-sm ring-1 ring-orange-200/50' 
                    : 'bg-white/40 border-transparent hover:bg-white/80 hover:border-gray-100'
                ]"
              >
                <div class="flex items-center gap-4">
                  <div class="relative">
                    <img :src="item.coverImage" class="w-16 h-16 rounded-xl object-cover shadow-sm bg-gray-100" />
                    <div v-if="selectedIndividualId === item.id" class="absolute -top-1 -right-1 w-4 h-4 bg-orange-500 rounded-full border-2 border-white animate-pulse"></div>
                  </div>
                  <div class="flex-1 overflow-hidden">
                    <div class="flex justify-between items-start mb-1">
                      <h4 class="font-bold text-gray-800 truncate">Individual #{{ item.id }}</h4>
                      <span class="text-xs text-gray-400 font-mono">Count: {{ item.count }}</span>
                    </div>
                    <div class="flex items-center text-[10px] text-gray-500 space-x-2">
                       <span class="flex items-center"><el-icon class="mr-1"><Clock /></el-icon>{{ formatShortDate(item.lastSeen) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- Main Section: Individual Detail -->
      <main class="flex-1 overflow-hidden relative">
        <div v-if="!selectedIndividualId" class="h-full flex flex-col items-center justify-center bg-white/40 backdrop-blur-sm border border-white/50 rounded-[2.5rem] shadow-soft-blue text-gray-400">
          <div class="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center mb-6">
            <el-icon size="40" class="opacity-30"><Histogram /></el-icon>
          </div>
          <p class="text-xl font-light">请从左侧选择一个个体以查看详细报告</p>
        </div>

        <div v-else class="h-full flex flex-col gap-6 overflow-hidden">
          <!-- Detail Header Card -->
          <div class="bg-white/60 backdrop-blur-md border border-white/50 rounded-[2.5rem] shadow-soft-blue p-8 animate-fade-in-up">
            <div v-if="detailsLoading" class="animate-pulse flex items-center space-x-6">
              <div class="w-24 h-24 bg-gray-200 rounded-3xl"></div>
              <div class="flex-1 space-y-4">
                <div class="h-6 bg-gray-200 rounded w-1/4"></div>
                <div class="h-4 bg-gray-200 rounded w-1/2"></div>
              </div>
            </div>
            
            <div v-else-if="individualDetails" class="flex flex-col md:flex-row gap-8 items-start">
              <div class="relative">
                <img :src="individualDetails.images[0]?.url" class="w-32 h-32 rounded-[2rem] object-cover shadow-lg ring-4 ring-white/80" />
                <div class="absolute -bottom-2 -right-2 bg-gradient-to-br from-orange-400 to-orange-600 text-white px-3 py-1 rounded-full text-xs font-bold shadow-md">
                   PRO
                </div>
              </div>
              
              <div class="flex-1">
                <div class="flex justify-between items-start mb-4">
                  <div>
                    <h2 class="text-3xl font-extrabold text-gray-800 mb-1">Individual #{{ selectedIndividualId }}</h2>
                    <div class="flex items-center space-x-3">
                      <span class="px-2 py-0.5 bg-blue-100 text-blue-600 rounded text-[10px] font-bold uppercase tracking-wider">Human Species</span>
                      <span class="text-gray-400 text-xs flex items-center">
                        <el-icon class="mr-1"><Location /></el-icon> Main Entrance Area
                      </span>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-xs text-gray-400 mb-1 uppercase tracking-widest font-semibold">Total Detections</p>
                    <p class="text-4xl font-black text-orange-600">{{ individualDetails.totalCount }}</p>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4 mt-6">
                   <div class="bg-white/40 p-3 rounded-2xl border border-white/50">
                      <p class="text-[10px] text-gray-400 mb-1">FIRST SEEN</p>
                      <p class="text-sm font-semibold text-gray-700">{{ formatDateTime(individualDetails.firstSeen) }}</p>
                   </div>
                   <div class="bg-white/40 p-3 rounded-2xl border border-white/50">
                      <p class="text-[10px] text-gray-400 mb-1">LAST RECORDED</p>
                      <p class="text-sm font-semibold text-gray-700">{{ formatDateTime(individualDetails.lastSeen) }}</p>
                   </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Gallery Area -->
          <div class="flex-1 bg-white/60 backdrop-blur-md border border-white/50 rounded-[2.5rem] shadow-soft-blue flex flex-col overflow-hidden animate-fade-in-up" style="animation-delay: 200ms;">
             <div class="p-6 border-b border-gray-100/50 flex justify-between items-center shrink-0">
                <h3 class="text-xl font-bold text-gray-800">识别踪迹回顾</h3>
                <div class="flex gap-2">
                   <el-button-group>
                      <el-button size="small" :icon="Grid" plain />
                      <el-button size="small" :icon="List" plain />
                   </el-button-group>
                </div>
             </div>
             
             <div class="flex-1 overflow-y-auto p-8 custom-scrollbar">
                <div v-if="detailsLoading" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6">
                   <div v-for="i in 10" :key="i" class="animate-pulse space-y-3">
                      <div class="aspect-square bg-gray-200 rounded-2xl"></div>
                      <div class="h-3 bg-gray-200 rounded w-3/4 mx-auto"></div>
                   </div>
                </div>

                <div v-else-if="individualDetails?.images?.length" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6">
                   <div 
                    v-for="image in individualDetails.images" 
                    :key="image.id" 
                    class="group relative bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
                   >
                      <div class="aspect-[4/5] relative overflow-hidden bg-gray-50">
                         <img :src="image.url" class="absolute inset-0 w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
                         <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-4">
                            <span class="text-blue-400 text-[10px] font-mono">ID: {{ image.id }}</span>
                         </div>
                         <div class="absolute top-3 right-3 bg-white/90 backdrop-blur-sm px-2 py-1 rounded-lg shadow-sm">
                            <span class="text-[10px] font-bold text-orange-600">{{ (image.confidence * 100).toFixed(1) }}%</span>
                         </div>
                      </div>
                      <div class="p-3">
                         <p class="text-[10px] text-gray-500 flex items-center">
                            <el-icon class="mr-1"><Timer /></el-icon> {{ formatShortTime(image.captureTime) }}
                         </p>
                         <p class="text-[10px] text-gray-400 mt-1 uppercase tracking-tighter">{{ formatShortDate(image.captureTime) }}</p>
                      </div>
                   </div>
                </div>
                
                <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 opacity-50">
                   <el-icon size="64"><Picture /></el-icon>
                   <p class="mt-4">未找到识别影像记录</p>
                </div>
             </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { 
  Search, 
  Refresh, 
  Loading, 
  UserFilled, 
  Clock, 
  Location, 
  Histogram, 
  Grid, 
  List, 
  Timer,
  Picture
} from '@element-plus/icons-vue';
import { getIndividualReport } from '@/services/modules/recognition.js';
import { ElMessage } from 'element-plus';

// State
const searchQuery = ref('');
const individuals = ref([]);
const loading = ref(false);
const selectedIndividualId = ref(null);
const individualDetails = ref(null);
const detailsLoading = ref(false);

// Filtered Individuals
const filteredIndividuals = computed(() => {
  if (!searchQuery.value) return individuals.value;
  const q = searchQuery.value.toLowerCase();
  return individuals.value.filter(item => item.id.toString().includes(q));
});

// Mock Function: Fetch List
const fetchIndividuals = async () => {
  loading.value = true;
  // Simulate API delay
  await new Promise(resolve => setTimeout(resolve, 800));
  
  // Create mock data
  const mockData = Array.from({ length: 12 }, (_, i) => ({
    id: 1000 + i,
    coverImage: `https://picsum.photos/seed/${1000 + i}/300/400`,
    firstSeen: new Date(Date.now() - Math.random() * 1000000000).toISOString(),
    lastSeen: new Date(Date.now() - Math.random() * 100000000).toISOString(),
    count: Math.floor(Math.random() * 50) + 1
  }));
  
  individuals.value = mockData.sort((a, b) => new Date(b.lastSeen) - new Date(a.lastSeen));
  loading.value = false;
};

// Mock Function: Fetch Individual Detail
const selectIndividual = async (id) => {
  selectedIndividualId.value = id;
  detailsLoading.value = true;
  
  try {
    // 调用真实 API
    const res = await getIndividualReport(id);
    const data = res?.data || res;
    
    if (data) {
      individualDetails.value = {
        id: data.individualId,
        firstSeen: data.images?.[0]?.shotTime ? new Date(data.images[0].shotTime).toISOString() : new Date().toISOString(),
        lastSeen: data.images?.[data.images.length - 1]?.shotTime ? new Date(data.images[data.images.length - 1].shotTime).toISOString() : new Date().toISOString(),
        totalCount: data.images?.length || 0,
        images: (data.images || []).map(img => ({
          id: img.imageId,
          url: toAbsoluteUrl(img.imagePath),
          captureTime: img.shotTime ? new Date(img.shotTime).toISOString() : new Date().toISOString(),
          confidence: 0.95, // 可以从后端返回，这里给默认值
          recordId: img.recognitionRecordId
        })).sort((a, b) => new Date(b.captureTime).getTime() - new Date(a.captureTime).getTime())
      };
      ElMessage.success('加载成功');
    }
  } catch (error) {
    console.error('Failed to fetch individual report:', error);
    ElMessage.error('加载失败，使用模拟数据');
    
    // Fallback to mock data
    const found = individuals.value.find(item => item.id === id);
    if (found) {
      const imagesCount = Math.floor(Math.random() * 15) + 5;
      individualDetails.value = {
        id: found.id,
        firstSeen: found.firstSeen,
        lastSeen: found.lastSeen,
        totalCount: found.count * 3, // Just for variety
        images: Array.from({ length: imagesCount }, (_, j) => ({
          id: id * 100 + j,
          url: `https://picsum.photos/seed/${id}_${j}/300/400`,
          captureTime: new Date(new Date(found.firstSeen).getTime() + (new Date(found.lastSeen).getTime() - new Date(found.firstSeen).getTime()) * Math.random()).toISOString(),
          confidence: 0.85 + Math.random() * 0.14
        })).sort((a, b) => new Date(b.captureTime).getTime() - new Date(a.captureTime).getTime())
      };
    }
  }
  
  detailsLoading.value = false;
};

// Format Helpers
const formatDateTime = (isoString) => {
  if (!isoString) return '---';
  return new Date(isoString).toLocaleString('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  });
};

const formatShortDate = (isoString) => {
  if (!isoString) return '---';
  return new Date(isoString).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  });
};

const formatShortTime = (isoString) => {
  if (!isoString) return '---';
  return new Date(isoString).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

const backendOrigin = computed(() => {
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  return apiBase.replace(/\/api\/?$/, '');
});

const toAbsoluteUrl = (maybePath) => {
  if (!maybePath) return null;
  if (/^https?:\/\//i.test(maybePath)) return maybePath;
  if (maybePath.startsWith('/')) return backendOrigin.value + maybePath;
  return backendOrigin.value + '/' + maybePath;
};

onMounted(() => {
  fetchIndividuals();
});
</script>

<style scoped>
.shadow-soft-blue {
  box-shadow: 0 20px 50px rgba(100, 150, 255, 0.15);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.1);
}

/* Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}

:deep(.custom-search-input .el-input__wrapper) {
  background-color: white !important;
  border-radius: 9999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.custom-search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 4px 15px rgba(242, 140, 56, 0.2) !important;
  border-color: #F28C38 !important;
}
</style>
