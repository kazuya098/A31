<template>
  <div class="page-root font-sans flex flex-col overflow-hidden">

    <!-- ───────────── Header ───────────── -->
    <header class="header-bar flex justify-between items-center shrink-0 animate-fade-in-down">
      <div class="flex flex-col gap-3">
        <div>
          <h1 class="page-title">
            个体档案
            <span class="page-title-accent">管理中心</span>
          </h1>
        </div>

        <!-- 提交按钮 -->
        <div class="flex items-center gap-3">
          <button class="submit-btn" @click="openSubmitDialog" :disabled="submitting">
            <el-icon v-if="submitting" class="animate-spin mr-2"><Loading /></el-icon>
            <el-icon v-else class="mr-2"><Upload /></el-icon>
            {{ submitting ? '提交中...' : '提交' }}
          </button>
          <span v-if="submitting" class="text-xs text-gray-400 animate-pulse">正在上传，请稍候…</span>
        </div>

        <!-- 隐藏的文件输入 -->
        <input
          ref="submitFileInput"
          type="file"
          accept="image/*"
          multiple
          class="hidden"
          @change="handleSubmitFiles"
        />
      </div>

      <div class="flex items-center gap-3">
        <el-input
          v-model="searchQuery"
          placeholder="搜索个体编号..."
          class="search-input w-56"
          clearable
        >
          <template #prefix>
            <el-icon class="text-gray-400"><Search /></el-icon>
          </template>
        </el-input>
        <el-button circle :icon="Refresh" @click="fetchIndividuals" class="refresh-btn" :loading="loading" />
      </div>
    </header>

    <!-- ───────────── Body ───────────── -->
    <div class="flex-1 flex gap-6 overflow-hidden pb-6 px-2">

      <!-- ── Sidebar: Individual List ── -->
      <aside class="sidebar flex flex-col gap-0 overflow-hidden">
        <div class="sidebar-card flex flex-col h-full overflow-hidden">
          <div class="sidebar-header">
            <h3 class="sidebar-heading">个体列表</h3>
            <span class="count-badge">{{ filteredIndividuals.length }} 个</span>
          </div>

          <div class="flex-1 overflow-y-auto px-3 py-3 custom-scrollbar space-y-2">
            <div v-if="loading" class="flex flex-col items-center justify-center h-full gap-4 opacity-60">
              <el-icon class="animate-spin text-orange-400" size="28"><Loading /></el-icon>
              <p class="text-gray-400 text-sm">加载中…</p>
            </div>

            <div v-else-if="filteredIndividuals.length === 0"
              class="flex flex-col items-center justify-center h-full text-gray-300 gap-3">
              <el-icon size="44"><UserFilled /></el-icon>
              <p class="text-sm">暂无个体档案</p>
            </div>

            <div
              v-for="item in filteredIndividuals"
              :key="item.individualId"
              @click="selectIndividual(item.individualId)"
              :class="[
                'individual-card',
                selectedId === item.individualId ? 'individual-card--active' : ''
              ]"
            >
              <div class="relative shrink-0">
                <img
                  :src="item.coverImagePath || '/examples/mandrill_1.jpg'"
                  class="w-14 h-14 rounded-2xl object-cover bg-gray-100 shadow-sm"
                  @error="(e) => e.target.src = '/examples/mandrill_1.jpg'"
                />
                <div v-if="selectedId === item.individualId"
                  class="absolute -top-1 -right-1 w-3.5 h-3.5 bg-orange-500 rounded-full border-2 border-white shadow-sm"></div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex justify-between items-baseline mb-0.5">
                  <span class="text-sm font-bold text-gray-800 truncate">编号 #{{ item.individualId }}</span>
                  <span class="text-[10px] font-semibold px-2 py-0.5 rounded-full ml-2 shrink-0"
                    :class="item.speciesType === 'human' ? 'bg-blue-50 text-blue-500' : 'bg-emerald-50 text-emerald-600'">
                    {{ item.speciesType === 'human' ? '人类' : '非人类' }}
                  </span>
                </div>
                <div class="flex items-center gap-2 text-[11px] text-gray-400">
                  <el-icon class="shrink-0"><Picture /></el-icon>
                  <span>{{ item.imageCount || 0 }} 张影像</span>
                  <span v-if="item.latestShotTime" class="text-gray-300">·</span>
                  <span v-if="item.latestShotTime">{{ formatShortDate(item.latestShotTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- ── Main Panel: Detail ── -->
      <main class="flex-1 flex flex-col gap-5 overflow-hidden min-w-0">

        <!-- Empty state -->
        <div v-if="!selectedId"
          class="h-full flex flex-col items-center justify-center empty-card gap-5 text-gray-300">
          <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center">
            <el-icon size="36" class="opacity-40"><Histogram /></el-icon>
          </div>
          <p class="text-base font-light text-gray-400">请从左侧选择一个个体以查看详细档案</p>
        </div>

        <template v-else>
          <!-- Detail Header Card -->
          <div class="detail-header-card animate-fade-in-up">
            <!-- Loading skeleton -->
            <div v-if="detailLoading" class="flex items-center gap-6 animate-pulse">
              <div class="w-28 h-28 bg-gray-200 rounded-[1.5rem] shrink-0"></div>
              <div class="flex-1 space-y-3">
                <div class="h-5 bg-gray-200 rounded w-1/3"></div>
                <div class="h-4 bg-gray-200 rounded w-1/2"></div>
                <div class="h-4 bg-gray-200 rounded w-1/4"></div>
              </div>
            </div>

            <div v-else-if="details" class="flex flex-col sm:flex-row gap-6 items-start">
              <!-- Cover image -->
              <div class="relative shrink-0">
                <img
                  :src="details.coverImage || details.images[0]?.url || '/examples/mandrill_1.jpg'"
                  class="w-28 h-28 rounded-[1.5rem] object-cover shadow-lg ring-4 ring-white/80 bg-gray-100"
                  @error="(e) => e.target.src = '/examples/mandrill_1.jpg'"
                />
                <div class="species-badge">
                  {{ currentIndividual?.speciesType === 'human' ? '人类' : '非人类' }}
                </div>
              </div>

              <!-- Info -->
              <div class="flex-1 min-w-0">
                <div class="flex justify-between items-start flex-wrap gap-3 mb-5">
                  <div>
                    <h2 class="detail-id-title">编号：<span class="text-orange-600">#{{ selectedId }}</span></h2>
                    <p class="text-sm text-gray-400 flex items-center gap-1 mt-1">
                      <el-icon><Location /></el-icon> 四川省成都市
                    </p>
                  </div>
                  <div class="text-right shrink-0">
                    <p class="stat-label">已记录的总目击数</p>
                    <p class="stat-value">{{ details.totalCount }}</p>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-3">
                  <div class="stat-card">
                    <p class="stat-card-label">初次目击</p>
                    <p class="stat-card-value">{{ formatDateTime(details.firstSeen) }}</p>
                  </div>
                  <div class="stat-card">
                    <p class="stat-card-label">最近一次目击</p>
                    <p class="stat-card-value">{{ formatDateTime(details.lastSeen) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Gallery Card -->
          <div class="gallery-card flex flex-col overflow-hidden flex-1 animate-fade-in-up" style="animation-delay:0.1s">
            <!-- Gallery Header (no icons) -->
            <div class="gallery-header shrink-0">
              <h3 class="gallery-title">识别踪迹回顾</h3>
            </div>

            <!-- Gallery Grid -->
            <div class="flex-1 overflow-y-auto p-5 custom-scrollbar">
              <!-- Skeleton -->
              <div v-if="detailLoading" class="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                <div v-for="i in 10" :key="i" class="animate-pulse space-y-2">
                  <div class="aspect-[4/5] bg-gray-200 rounded-2xl"></div>
                  <div class="h-3 bg-gray-200 rounded w-2/3 mx-auto"></div>
                </div>
              </div>

              <!-- Images -->
              <div v-else-if="details?.images?.length"
                class="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                <div
                  v-for="img in details.images"
                  :key="img.id"
                  class="photo-card group"
                >
                  <div class="aspect-[4/5] relative overflow-hidden rounded-xl bg-gray-100">
                    <img
                      :src="img.url"
                      class="absolute inset-0 w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                      @error="(e) => e.target.src = '/examples/mandrill_1.jpg'"
                    />
                    <!-- Overlay -->
                    <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    <!-- Confidence badge -->
                    <div v-if="img.confidence" class="conf-badge">
                      {{ (img.confidence * 100).toFixed(0) }}%
                    </div>
                  </div>
                  <div class="px-1 pt-2 pb-1">
                    <p class="text-[10px] text-gray-500 font-medium">
                      {{ formatShortDate(img.captureTime) }}
                    </p>
                    <p class="text-[9px] text-gray-400 mt-0.5">
                      {{ formatShortTime(img.captureTime) }}
                    </p>
                  </div>
                </div>
              </div>

              <!-- Empty -->
              <div v-else class="h-full flex flex-col items-center justify-center text-gray-300 gap-3 py-16">
                <el-icon size="52"><Picture /></el-icon>
                <p class="text-sm">暂无识别影像记录</p>
              </div>
            </div>
          </div>
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import {
  Search, Refresh, Loading, UserFilled,
  Location, Histogram, Picture, Upload
} from '@element-plus/icons-vue';
import { getIndividuals, getIndividualReport, batchUploadImages } from '@/services/modules/recognition.js';
import { ElMessage } from 'element-plus';

// ─── State ───────────────────────────────────────────────
const searchQuery = ref('');
const individuals = ref([]);
const loading = ref(false);
const selectedId = ref(null);
const details = ref(null);
const detailLoading = ref(false);
const submitting = ref(false);
const submitFileInput = ref(null);

// ─── Computed ────────────────────────────────────────────
const filteredIndividuals = computed(() => {
  if (!searchQuery.value) return individuals.value;
  const q = searchQuery.value.toLowerCase();
  return individuals.value.filter(item =>
    String(item.individualId).includes(q)
  );
});

const currentIndividual = computed(() =>
  individuals.value.find(i => i.individualId === selectedId.value)
);

// ─── Fetch individual list ────────────────────────────────
const fetchIndividuals = async () => {
  loading.value = true;
  try {
    const res = await getIndividuals();
    const data = res?.data || res;
    if (Array.isArray(data)) {
      individuals.value = data;
    }
  } catch (err) {
    console.error('Failed to fetch individuals:', err);
    ElMessage.error('个体列表加载失败');
  } finally {
    loading.value = false;
  }
};

// ─── Select & load detail ─────────────────────────────────
const selectIndividual = async (id) => {
  if (selectedId.value === id && details.value) return;
  selectedId.value = id;
  detailLoading.value = true;
  details.value = null;

  try {
    const res = await getIndividualReport(id);
    const data = res?.data || res;
    if (data) {
      const images = (data.images || []).map(img => ({
        id: img.imageId,
        url: toAbsoluteUrl(img.imagePath),
        captureTime: img.shotTime ? new Date(img.shotTime).toISOString() : null,
        confidence: 0.95,
        recordId: img.recordId,
      })).sort((a, b) => {
        if (!a.captureTime) return 1;
        if (!b.captureTime) return -1;
        return new Date(b.captureTime) - new Date(a.captureTime);
      });

      const validTimes = images.filter(i => i.captureTime).map(i => new Date(i.captureTime));
      details.value = {
        id: data.individualId,
        coverImage: toAbsoluteUrl(data.coverImagePath),
        totalCount: data.totalImages || images.length,
        firstSeen: data.firstSeenDate ? new Date(data.firstSeenDate).toISOString() : (validTimes.length ? new Date(Math.min(...validTimes)).toISOString() : null),
        lastSeen: data.lastSeenDate ? new Date(data.lastSeenDate).toISOString() : (validTimes.length ? new Date(Math.max(...validTimes)).toISOString() : null),
        images,
      };
    }
  } catch (err) {
    console.error('Failed to load individual detail:', err);
    ElMessage.error('档案加载失败');
    details.value = { id, totalCount: 0, firstSeen: null, lastSeen: null, images: [] };
  } finally {
    detailLoading.value = false;
  }
};

// ─── Submit (batch upload) ────────────────────────────────
const openSubmitDialog = () => {
  submitFileInput.value?.click();
};

const handleSubmitFiles = async (e) => {
  const files = Array.from(e.target.files);
  if (!files.length) return;
  if (files.length > 10) {
    ElMessage.warning('每次最多提交 10 张图片');
    e.target.value = '';
    return;
  }

  submitting.value = true;
  try {
    const form = new FormData();
    files.forEach(f => form.append('files', f));
    form.append('type', 'non_human');
    const today = new Date().toISOString().split('T')[0];
    files.forEach(() => form.append('shotTimes', today));

    await batchUploadImages(form);
    ElMessage.success({ message: '提交成功', type: 'success', duration: 3000 });
    await fetchIndividuals();
  } catch (err) {
    console.error('Batch upload failed:', err);
    ElMessage.error('提交失败，请重试');
  } finally {
    submitting.value = false;
    e.target.value = '';
  }
};

// ─── URL helpers ──────────────────────────────────────────
const backendOrigin = computed(() => {
  const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  return base.replace(/\/api\/?$/, '');
});

const toAbsoluteUrl = (p) => {
  if (!p) return null;
  if (/^https?:\/\//i.test(p)) return p;
  if (p.startsWith('/')) return backendOrigin.value + p;
  return backendOrigin.value + '/' + p;
};

// ─── Formatters ───────────────────────────────────────────
const formatDateTime = (iso) => {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('zh-CN', { dateStyle: 'medium', timeStyle: 'short' });
};

const formatShortDate = (iso) => {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' });
};

const formatShortTime = (iso) => {
  if (!iso) return '';
  return new Date(iso).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

onMounted(fetchIndividuals);
</script>

<style scoped>
/* ─── Page root ─────────────────────────────────────────── */
.page-root {
  height: 100%;
  background: #F7F4EF;
  gap: 0;
}

/* ─── Header ─────────────────────────────────────────────── */
.header-bar {
  padding: 20px 8px 20px 8px;
}

.page-title {
  font-size: 2rem;
  font-weight: 300;
  letter-spacing: -0.02em;
  color: #111827;
  margin: 0;
  line-height: 1.2;
}

.page-title-accent {
  font-weight: 700;
  color: #ea580c;
}

/* ─── Submit button ─────────────────────────────────────── */
.submit-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 28px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.35);
  transition: all 0.25s ease;
}
.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.45);
}
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ─── Search input ───────────────────────────────────────── */
:deep(.search-input .el-input__wrapper) {
  border-radius: 9999px !important;
  background: #fff !important;
  border: 1px solid rgba(0, 0, 0, 0.07) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
  padding: 6px 16px !important;
}
:deep(.search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.25) !important;
  border-color: #f97316 !important;
}

/* ─── Refresh button ─────────────────────────────────────── */
:deep(.refresh-btn) {
  background: #fff !important;
  border: 1px solid rgba(0,0,0,0.08) !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04) !important;
  color: #6b7280 !important;
}

/* ─── Sidebar ────────────────────────────────────────────── */
.sidebar {
  width: 300px;
  min-width: 260px;
  height: 100%;
}

.sidebar-card {
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.6);
  border-radius: 1.75rem;
  box-shadow: 0 8px 32px rgba(100,130,200,0.1), 0 1px 4px rgba(0,0,0,0.04);
  overflow: hidden;
}

.sidebar-header {
  padding: 18px 20px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-heading {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f2937;
}

.count-badge {
  padding: 3px 10px;
  background: #fff7ed;
  color: #ea580c;
  border-radius: 9999px;
  font-size: 0.7rem;
  font-weight: 600;
}

/* ─── Individual card ────────────────────────────────────── */
.individual-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 12px;
  border-radius: 1rem;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}
.individual-card:hover {
  background: rgba(255,255,255,0.9);
  border-color: rgba(0,0,0,0.06);
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.individual-card--active {
  background: #fff7ed !important;
  border-color: rgba(249,115,22,0.2) !important;
  box-shadow: 0 4px 14px rgba(249,115,22,0.12) !important;
}

/* ─── Empty / main cards ─────────────────────────────────── */
.empty-card {
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.6);
  border-radius: 2rem;
  box-shadow: 0 8px 32px rgba(100,130,200,0.08);
}

/* ─── Detail header card ─────────────────────────────────── */
.detail-header-card {
  background: rgba(255,255,255,0.82);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.65);
  border-radius: 2rem;
  box-shadow: 0 8px 32px rgba(100,130,200,0.1), 0 1px 4px rgba(0,0,0,0.04);
  padding: 24px 28px;
}

.species-badge {
  position: absolute;
  bottom: -8px;
  right: -8px;
  padding: 3px 10px;
  border-radius: 9999px;
  font-size: 0.65rem;
  font-weight: 700;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff;
  box-shadow: 0 2px 8px rgba(249,115,22,0.4);
}

.detail-id-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: #111827;
  letter-spacing: -0.02em;
}

/* ─── Stats ──────────────────────────────────────────────── */
.stat-label {
  font-size: 0.6rem;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 2px;
}
.stat-value {
  font-size: 2.5rem;
  font-weight: 900;
  color: #ea580c;
  line-height: 1;
}

.stat-card {
  background: rgba(249,250,251,0.8);
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 1rem;
  padding: 12px 14px;
}
.stat-card-label {
  font-size: 0.6rem;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}
.stat-card-value {
  font-size: 0.8rem;
  font-weight: 600;
  color: #374151;
}

/* ─── Gallery card ───────────────────────────────────────── */
.gallery-card {
  background: rgba(255,255,255,0.82);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.65);
  border-radius: 2rem;
  box-shadow: 0 8px 32px rgba(100,130,200,0.1), 0 1px 4px rgba(0,0,0,0.04);
  min-height: 0;
}

.gallery-header {
  padding: 18px 24px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}

.gallery-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1f2937;
}

/* ─── Photo cards ────────────────────────────────────────── */
.photo-card {
  cursor: default;
  transition: transform 0.2s ease;
}
.photo-card:hover {
  transform: translateY(-3px);
}

.conf-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(6px);
  color: #ea580c;
  font-size: 0.6rem;
  font-weight: 800;
  padding: 2px 7px;
  border-radius: 9999px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

/* ─── Animations ─────────────────────────────────────────── */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0);    }
}
.animate-fade-in-up {
  animation: fadeInUp 0.45s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-12px); }
  to   { opacity: 1; transform: translateY(0);     }
}
.animate-fade-in-down {
  animation: fadeInDown 0.4s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

/* ─── Scrollbar ──────────────────────────────────────────── */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.08);
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(0,0,0,0.15);
}
</style>
