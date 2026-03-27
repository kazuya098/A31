<template>
  <div class="h-full font-sans bg-[#F7F4EF] flex flex-col overflow-x-hidden overflow-y-auto relative">
    <!-- Background Decorative Elements -->
    <div class="absolute top-[-10%] left-[-5%] w-[40%] h-[40%] bg-orange-200/20 blur-[120px] rounded-full animate-blob pointer-events-none"></div>
    <div class="absolute bottom-[-10%] right-[-5%] w-[45%] h-[45%] bg-blue-200/20 blur-[120px] rounded-full animate-blob animation-delay-2000 pointer-events-none"></div>
    <div class="absolute top-[20%] right-[10%] w-[20%] h-[20%] bg-purple-200/10 blur-[100px] rounded-full animate-blob animation-delay-4000 pointer-events-none"></div>

    <!-- Header -->
    <header class="flex justify-between items-center mb-16 py-8 px-12 shrink-0 z-10 animate-fade-in-down">
      <div>
        <h1 class="text-4xl font-light tracking-tight text-gray-900 mb-2">
          上传识别 <span class="font-semibold text-orange-600">分析中心</span>
        </h1>
        <p class="text-gray-500 text-lg">利用跨时域 AI 算法进行特征索引与生物个体建模</p>
      </div>
      <div class="flex items-center space-x-6">
        <div class="flex flex-col items-end mr-4">
           <span class="text-[10px] uppercase font-bold text-gray-400 tracking-widest mb-1">Recognition Engine</span>
           <span class="text-xs font-mono text-orange-600 bg-orange-50 px-2 py-0.5 rounded border border-orange-100 italic">Antigravity-V3.8</span>
        </div>
        <el-radio-group v-model="recognitionType" class="custom-radio-group">
          <el-radio-button label="human">人类识别</el-radio-button>
          <el-radio-button label="non_human">非人识别</el-radio-button>
        </el-radio-group>
      </div>
    </header>

    <!-- Main Content Area -->
    <div class="flex-1 flex px-12 pb-12 gap-10 overflow-x-hidden overflow-y-auto z-10">
      
      <!-- Left: Primary Interaction Area -->
      <div class="flex-1 flex flex-col overflow-x-hidden overflow-y-auto">
        
        <!-- Upload State -->
        <div 
          v-if="status === 'idle' || status === 'uploading'"
          class="flex-1 flex flex-col items-center justify-center"
        >
          <div 
            @dragover.prevent="dragOver = true"
            @dragleave.prevent="dragOver = false"
            @drop.prevent="handleDrop"
            :class="[
              'w-full bg-white/40 backdrop-blur-xl border-2 border-dashed rounded-[4rem] shadow-soft-blue flex flex-col items-center justify-center p-24 transition-all duration-700 relative overflow-hidden group',
              dragOver ? 'border-orange-500 bg-orange-50/50 scale-[1.01]' : 'border-white/60 bg-white/40',
              status === 'uploading' ? 'opacity-60 pointer-events-none' : 'hover:bg-white/60 hover:border-orange-200'
            ]"
          >
            <!-- Decorative circle in background -->
            <div class="absolute -top-24 -right-24 w-64 h-64 bg-orange-50 rounded-full opacity-50 group-hover:scale-110 transition-transform duration-1000"></div>
            
            <div class="relative z-10 flex flex-col items-center">
              <div class="w-32 h-32 bg-gradient-to-br from-orange-400 to-orange-600 rounded-3xl flex items-center justify-center mb-10 shadow-lg group-hover:rotate-6 transition-transform">
                <el-icon size="56" class="text-white"><UploadFilled /></el-icon>
              </div>
              <h2 class="text-4xl font-black text-gray-800 mb-6 tracking-tight">拖拽影像至此开始识别</h2>
              <p class="text-gray-500 text-center max-w-lg mb-12 text-lg leading-relaxed font-light">
                系统将自动为您分配边缘计算节点，进行跨地域、跨时域的特征比对与生物多样性数据同步。
              </p>
              
              <div class="flex gap-4">
                 <el-button 
                  type="primary" 
                  size="large" 
                  class="premium-upload-btn"
                  @click="openFileDialog"
                  :loading="status === 'uploading'"
                >
                  <el-icon class="mr-2"><Plus /></el-icon> 选择图像文件
                </el-button>
                <input type="file" ref="fileInput" class="hidden" accept="image/*" @change="handleFileChange">
              </div>

              <div class="mt-12 flex items-center gap-8 opacity-40">
                 <div class="flex flex-col items-center"><el-icon size="24"><Picture /></el-icon><span class="text-[10px] mt-1 font-bold">RAW / JPG</span></div>
                 <div class="flex flex-col items-center"><el-icon size="24"><Monitor /></el-icon><span class="text-[10px] mt-1 font-bold">100% SECURE</span></div>
                 <div class="flex flex-col items-center"><el-icon size="24"><Cpu /></el-icon><span class="text-[10px] mt-1 font-bold">GPU ACCEL</span></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Processing State -->
        <div 
          v-if="status === 'processing'"
          class="flex-1 flex flex-col items-center justify-center animate-fade-in-up"
        >
          <div class="w-full max-w-3xl bg-white/60 backdrop-blur-xl border border-white/50 rounded-[4rem] shadow-soft-blue p-20 flex flex-col items-center">
             <div class="relative w-64 h-64 mb-16">
                <!-- Circular progress logic simplified -->
                <svg class="w-full h-full transform -rotate-90">
                  <circle cx="128" cy="128" r="110" stroke="currentColor" stroke-width="12" fill="transparent" class="text-orange-50" />
                  <circle cx="128" cy="128" r="110" stroke="currentColor" stroke-width="12" fill="transparent" class="text-orange-500" 
                    :stroke-dasharray="691" :stroke-dashoffset="691 - (691 * progress / 100)" stroke-linecap="round" />
                </svg>
                <div class="absolute inset-0 flex flex-col items-center justify-center">
                   <span class="text-6xl font-black text-gray-800 mb-1">{{ progress }}%</span>
                   <span class="text-[10px] font-bold text-gray-400 tracking-widest uppercase">Analyzing</span>
                </div>
                <div class="absolute -inset-10 bg-orange-400/10 blur-[60px] rounded-full z-[-1] animate-pulse"></div>
             </div>

             <div class="w-full space-y-8">
                <div class="flex justify-between items-center px-4">
                   <h3 class="text-2xl font-bold text-gray-800">{{ currentStepLabel }}...</h3>
                   <el-tag type="warning" effect="dark" round size="small" class="animate-pulse">ENGAGED</el-tag>
                </div>
                
                <div class="grid grid-cols-3 gap-6">
                   <div v-for="(step, idx) in steps" :key="idx" class="flex flex-col items-center transition-all duration-300" :class="[idx <= currentStep ? 'opacity-100 scale-100' : 'opacity-30 scale-95']">
                      <div :class="['w-10 h-10 rounded-full flex items-center justify-center mb-3', idx < currentStep ? 'bg-orange-500 text-white' : 'bg-orange-100 text-orange-600']">
                         <el-icon v-if="idx < currentStep"><Check /></el-icon>
                         <el-icon v-else class="animate-spin"><Loading v-if="idx === currentStep" /></el-icon>
                      </div>
                      <span class="text-xs font-bold text-gray-600 truncate">{{ step }}</span>
                   </div>
                </div>
             </div>
          </div>
        </div>

        <!-- Completed Result State -->
        <div 
          v-if="status === 'completed' && result"
          class="flex-1 flex flex-col gap-8 animate-fade-in-up overflow-hidden"
        >
          <div class="grid grid-cols-12 gap-8 h-full">
             <!-- Result Visualization -->
             <div class="col-span-8 bg-white/60 backdrop-blur-xl border border-white/50 rounded-[4rem] shadow-soft-blue p-10 flex flex-col">
                <div class="flex justify-between items-center mb-8 shrink-0">
                  <div class="flex items-center gap-4">
                    <div class="w-10 h-10 bg-orange-500 rounded-2xl flex items-center justify-center text-white shadow-lg shadow-orange-200">
                      <el-icon size="20"><Picture /></el-icon>
                    </div>
                    <h3 class="text-2xl font-black text-gray-800">视觉分析报告</h3>
                  </div>
                  <div class="flex items-center gap-6">
                     <span class="text-xs font-bold text-gray-400 uppercase tracking-widest">Heatmap Filter</span>
                     <el-switch v-model="showHeatmap" class="custom-orange-switch" />
                  </div>
                </div>

                <div class="flex-1 relative rounded-[2.5rem] overflow-hidden bg-gray-950 shadow-2xl group border-4 border-white/50">
                   <img :src="result.imageUrl" class="w-full h-full object-contain transition-transform duration-1000 group-hover:scale-105" />
                   
                   <transition name="fade">
                      <div v-if="showHeatmap" class="absolute inset-0 pointer-events-none">
                         <img
                           v-if="heatmapUrl"
                           :src="heatmapUrl"
                           class="w-full h-full object-contain opacity-80 mix-blend-screen"
                         />
                         <div v-else class="w-full h-full bg-heatmap opacity-80 mix-blend-screen"></div>
                         <!-- Dynamic focal points -->
                         <div class="absolute top-[30%] left-[45%] w-32 h-32 border border-white/30 rounded-full animate-ping-slow"></div>
                         <div class="absolute bottom-[20%] right-[30%] w-20 h-20 border border-white/20 rounded-full animate-ping-slow animation-delay-1000"></div>
                      </div>
                   </transition>

                   <div v-if="showHeatmap" class="absolute inset-0 bg-gradient-to-t from-orange-500/10 to-transparent pointer-events-none"></div>
                   <div v-if="showHeatmap" class="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-transparent via-orange-400 to-transparent shadow-[0_0_30px_rgba(251,146,60,1)] animate-scan-faster z-20"></div>

                   <div class="absolute bottom-6 left-6 flex gap-3 z-30">
                      <span class="px-3 py-1 bg-black/40 backdrop-blur-md border border-white/20 text-white rounded-lg text-[10px] font-mono">LAT: 39.9042° N</span>
                      <span class="px-3 py-1 bg-black/40 backdrop-blur-md border border-white/20 text-white rounded-lg text-[10px] font-mono">LNG: 116.4074° E</span>
                   </div>
                </div>

                <div class="mt-8 flex justify-center gap-6 shrink-0">
                   <el-button size="large" @click="reset" :icon="RefreshLeft" class="round-btn">放弃并重试</el-button>
                   <el-button size="large" type="primary" :icon="View" @click="goDetails" class="round-btn glow-btn">同步至个体档案</el-button>
                </div>
             </div>

             <!-- Summary Info Card -->
             <div class="col-span-4 flex flex-col gap-8">
                <div class="bg-gradient-to-br from-orange-500 to-red-600 rounded-[3.5rem] p-10 text-white shadow-soft-blue flex flex-col">
                   <p class="text-[10px] font-black uppercase tracking-[0.2em] opacity-70 mb-2">Confidence Level</p>
                   <div class="flex items-baseline gap-2 mb-6">
                      <span class="text-8xl font-black tracking-tighter">{{ (result.confidence * 100).toFixed(0) }}</span>
                      <span class="text-3xl font-bold">%</span>
                   </div>
                   <div class="w-full bg-black/10 h-3 rounded-full overflow-hidden border border-white/10 p-0.5">
                      <div class="h-full bg-white rounded-full shadow-[0_0_15px_white]" :style="{ width: result.confidence * 100 + '%' }"></div>
                   </div>
                   <p class="mt-6 text-xs font-medium opacity-80 leading-relaxed italic">
                     "基于 VGG-19 编码器的特征向量距离计算结果，置信度极高，符合行业标准。"
                   </p>
                </div>

                <div class="flex-1 bg-white/50 backdrop-blur-xl border border-white/50 rounded-[3.5rem] p-10 shadow-soft-blue flex flex-col gap-8">
                   <div class="flex flex-col gap-1">
                      <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Recognized Identity</p>
                      <h4 class="text-4xl font-black text-gray-900 italic">ID#{{ result.individualId }}</h4>
                   </div>

                   <div class="space-y-6">
                      <div class="flex items-center gap-4">
                         <div class="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-500"><el-icon><Calendar /></el-icon></div>
                         <div class="flex-1">
                            <p class="text-[10px] text-gray-400 font-bold uppercase">Record Time</p>
                            <p class="text-sm font-bold text-gray-700">2026-03-23 22:15</p>
                         </div>
                      </div>
                      <div class="flex items-center gap-4">
                         <div class="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-500"><el-icon><Location /></el-icon></div>
                         <div class="flex-1">
                            <p class="text-[10px] text-gray-400 font-bold uppercase">Target Location</p>
                            <p class="text-sm font-bold text-gray-700">Zone-A / Perimeter 4</p>
                         </div>
                      </div>
                   </div>

                   <div class="mt-auto bg-gray-950/5 p-6 rounded-[2rem] border border-gray-200/20">
                      <h5 class="text-xs font-black text-gray-400 uppercase mb-3 flex items-center gap-2">
                         <el-icon class="text-orange-500"><InfoFilled /></el-icon> 诊断建议
                      </h5>
                      <p class="text-[11px] text-gray-500 leading-relaxed">
                         检测到该个体在近 30 天内曾出现于 4 个不同的时钟扇区。建议核对 <b>个体档案</b> 中的跨域移动路径。
                      </p>
                   </div>
                </div>
             </div>
          </div>
        </div>

      </div>

      <!-- Right: History Sidebar -->
      <aside class="w-96 flex flex-col gap-8 overflow-x-hidden overflow-y-auto">
         <div class="bg-white/40 backdrop-blur-xl border border-white/60 rounded-[3.5rem] shadow-soft-blue flex flex-col h-full overflow-hidden transition-all duration-500 px-2">
            <div class="p-8 pb-4 flex justify-between items-center">
               <h3 class="text-xl font-black text-gray-800">最近任务</h3>
               <el-badge :value="history.length" type="warning" />
            </div>
            
            <div class="flex-1 overflow-y-auto custom-scrollbar p-6 space-y-5">
               <div 
                v-for="(item, idx) in history" 
                :key="idx"
                class="bg-white/40 p-5 rounded-[2rem] border border-white/40 hover:bg-white/80 transition-all duration-300 cursor-pointer group hover:shadow-lg hover:-translate-y-1"
                @click="status === 'idle' ? loadHistory(item) : null"
               >
                  <div class="flex gap-4 items-center">
                     <div class="w-16 h-16 rounded-2xl overflow-hidden bg-gray-100 shadow-sm">
                        <img :src="item.imageUrl" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                     </div>
                     <div class="flex-1 min-w-0">
                        <div class="flex justify-between items-start mb-1">
                           <span class="text-xs font-black text-gray-900">#{{ item.individualId }}</span>
                           <span class="text-[9px] font-bold text-orange-500 px-2 py-0.5 bg-orange-50 rounded-full">{{ (item.confidence*100).toFixed(0) }}%</span>
                        </div>
                        <p class="text-[10px] text-gray-400 font-mono truncate">{{ item.taskId }}</p>
                        <p class="text-[10px] text-gray-500 mt-2 flex items-center gap-1 opacity-60"><el-icon><Clock /></el-icon> 2小时前</p>
                     </div>
                  </div>
               </div>

               <div v-if="history.length === 0" class="h-full flex flex-col items-center justify-center opacity-30 italic text-gray-400">
                  <el-icon size="40" class="mb-2"><CollectionTag /></el-icon>
                  <p>暂无历史记录</p>
               </div>
            </div>

            <div class="p-8 pt-0">
               <div class="bg-gradient-to-br from-indigo-50 to-blue-50 p-6 rounded-[2rem] border border-blue-100 flex flex-col items-center text-center">
                  <el-icon size="24" class="text-blue-500 mb-3"><Opportunity /></el-icon>
                  <p class="text-[10px] font-bold text-blue-900 uppercase tracking-widest mb-1">Quota Status</p>
                  <p class="text-xs text-blue-600">本月剩余额度: 4,302 / 5,000</p>
               </div>
            </div>
         </div>
      </aside>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { 
  UploadFilled, 
  Loading, 
  RefreshLeft, 
  View, 
  User, 
  Picture,
  Plus,
  Monitor,
  Cpu,
  Check,
  Clock,
  CollectionTag,
  Opportunity,
  Calendar,
  Location,
  InfoFilled
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { uploadImage } from '@/services/modules/recognition.js';

const router = useRouter();

// Config
const steps = ['载入影像', '特征提取', '数据库比对', '个体索引', '报告生成'];
const history = ref([
  { individualId: 1024, taskId: 'TASK-XW290-A', confidence: 0.98, imageUrl: 'https://picsum.photos/seed/h1/200/200' },
  { individualId: 1056, taskId: 'TASK-KJ112-B', confidence: 0.85, imageUrl: 'https://picsum.photos/seed/h2/200/200' },
  { individualId: 1102, taskId: 'TASK-ZZ003-C', confidence: 0.92, imageUrl: 'https://picsum.photos/seed/h3/200/200' },
]);

// State
const recognitionType = ref('human');
const dragOver = ref(false);
const status = ref('idle'); // idle, uploading, processing, completed
const progress = ref(0);
const fileInput = ref(null);
const showHeatmap = ref(false);
const result = ref(null);
const heatmapUrl = ref(null);

const currentStep = computed(() => {
  if (progress.value < 20) return 0;
  if (progress.value < 40) return 1;
  if (progress.value < 70) return 2;
  if (progress.value < 90) return 3;
  return 4;
});

const currentStepLabel = computed(() => steps[currentStep.value]);

// Methods
const openFileDialog = () => {
  if (status.value === 'uploading' || status.value === 'processing') return;
  fileInput.value?.click();
};

const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (file) startProcess(file);
};

const handleDrop = (e) => {
  dragOver.value = false;
  const file = e.dataTransfer.files[0];
  if (file) startProcess(file);
};

const backendOrigin = computed(() => {
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  // e.g. http://localhost:8080/api -> http://localhost:8080
  return apiBase.replace(/\/api\/?$/, '');
});

const toAbsoluteUrl = (maybePath) => {
  if (!maybePath) return null;
  if (/^https?:\/\//i.test(maybePath)) return maybePath;
  if (maybePath.startsWith('/')) return backendOrigin.value + maybePath;
  return backendOrigin.value + '/' + maybePath;
};

const startProcess = async (file) => {
  if (status.value !== 'idle') return;

  status.value = 'uploading';
  progress.value = 5;
  heatmapUrl.value = null;
  showHeatmap.value = false;

  // 先展示本地预览（避免网络慢时空白）
  const localPreviewUrl = URL.createObjectURL(file);
  result.value = {
    taskId: '',
    individualId: '-',
    confidence: 0,
    imageUrl: localPreviewUrl,
    reportId: null,
  };

  try {
    const form = new FormData();
    form.append('file', file);
    form.append('type', recognitionType.value);

    status.value = 'processing';
    progress.value = 25;

    const res = await uploadImage(form);
    const data = res?.data || res;

    progress.value = 90;

    const imageUrl = toAbsoluteUrl(data?.imagePath);
    const hmUrl = toAbsoluteUrl(data?.heatmapPath);

    result.value = {
      taskId: data?.taskId || '',
      individualId: data?.individualId ?? data?.identityId ?? '-',
      confidence: (typeof data?.confidence === 'number' ? data.confidence : 0),
      imageUrl: imageUrl || localPreviewUrl,
      reportId: data?.taskId ? Number(data.taskId) : null,
    };
    heatmapUrl.value = hmUrl;
    status.value = 'completed';
    progress.value = 100;

    if (hmUrl) {
      ElMessage.success('识别完成（已生成注意力热力图）');
    } else {
      ElMessage.success('识别完成');
    }
  } catch (e) {
    console.error(e);
    status.value = 'completed';
    progress.value = 100;
    ElMessage.error('识别失败，请重试');
  }
};

const loadHistory = (item) => {
   status.value = 'completed';
   result.value = item;
   heatmapUrl.value = item.heatmapUrl || null;
   showHeatmap.value = false;
};

const reset = () => {
  status.value = 'idle';
  result.value = null;
  heatmapUrl.value = null;
  showHeatmap.value = false;
  progress.value = 0;
};

const goDetails = () => {
  router.push('/individuals');
};
</script>

<style scoped>
.shadow-soft-blue {
  box-shadow: 0 40px 100px rgba(100, 150, 255, 0.1);
}

.premium-upload-btn {
  padding: 1.8rem 4rem;
  border-radius: 2rem;
  font-size: 1.25rem;
  font-weight: 900;
  letter-spacing: 0.02em;
  background: linear-gradient(135deg, #F28C38 0%, #EA580C 100%);
  border: none;
  box-shadow: 0 15px 40px rgba(242, 140, 56, 0.4);
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}

.premium-upload-btn:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 25px 50px rgba(242, 140, 56, 0.5);
}

.round-btn {
  border-radius: 9999px;
  padding-left: 2rem;
  padding-right: 2rem;
}

.glow-btn {
  box-shadow: 0 8px 20px rgba(242, 140, 56, 0.3);
}

.custom-radio-group :deep(.el-radio-button__inner) {
  background: white/30;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 1.5rem !important;
  margin: 0 6px;
  padding: 12px 24px;
  font-weight: bold;
  color: #666;
  transition: all 0.3s;
}

.custom-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #F28C38;
  color: white;
  border-color: transparent;
  box-shadow: 0 8px 20px rgba(242, 140, 56, 0.4);
}

/* Heatmap Effect */
.bg-heatmap {
  background: radial-gradient(circle at 45% 35%, rgba(255, 0, 0, 0.9), transparent 35%),
              radial-gradient(circle at 55% 55%, rgba(255, 255, 0, 0.7), transparent 25%),
              radial-gradient(circle at 35% 65%, rgba(0, 255, 255, 0.5), transparent 45%);
  filter: blur(50px);
}

@keyframes scanFaster {
  0% { transform: translateY(0); }
  50% { transform: translateY(600px); }
  100% { transform: translateY(0); }
}
.animate-scan-faster {
  animation: scanFaster 2.5s ease-in-out infinite;
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
.animate-blob {
  animation: blob 12s infinite alternate ease-in-out;
}
.animation-delay-2000 { animation-delay: 2s; }
.animation-delay-4000 { animation-delay: 4s; }

.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(50px); }
  to { opacity: 1; transform: translateY(0); }
}

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.05); border-radius: 10px; }

:deep(.custom-orange-switch.is-checked .el-switch__core) {
  background-color: #F28C38;
  border-color: #F28C38;
}

@keyframes pingSlow {
  0% { transform: scale(0.8); opacity: 0; }
  50% { transform: scale(1.2); opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}
.animate-ping-slow {
  animation: pingSlow 3s cubic-bezier(0, 0, 0.2, 1) infinite;
}
.animation-delay-1000 { animation-delay: 1s; }
</style>
