<template>
  <div class="h-full font-sans">
    <main class="h-full flex flex-col pt-4 px-8">
      <!-- 页面头部 -->
      <header class="flex justify-between items-center mb-16 animate-fade-in-down">
        <div>
          <h1 class="text-4xl font-light tracking-tight text-gray-900 mb-2">
            识别记录 <span class="font-semibold text-orange-600">管理中心</span>
          </h1>
          <p class="text-gray-500 text-lg">查看和管理所有识别记录</p>
        </div>
      </header>

      <!-- 搜索 / 筛选工具栏 -->
      <div class="bg-white/60 backdrop-blur-md border border-white/50 p-6 rounded-2xl shadow-soft-blue mb-8 animate-fade-in-up">
        <div class="flex items-center gap-4 flex-wrap">
          <!-- 记录ID搜索 -->
          <el-input
            v-model="filters.recordId"
            placeholder="按记录ID搜索"
            clearable
            class="!w-48"
            :prefix-icon="Search"
          />

          <!-- 识别类型筛选 -->
          <el-select v-model="filters.type" placeholder="识别类型" clearable class="!w-40">
            <el-option label="全部类型" value="" />
            <el-option label="人类识别" value="human" />
            <el-option label="非人类识别" value="non_human" />
          </el-select>

          <!-- 日期范围 -->
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="!w-72"
          />

          <!-- 搜索按钮 -->
          <button @click="fetchRecords"
            @mousemove="handleSearchMouseMove" @mouseleave="handleSearchMouseLeave"
            class="relative isolate overflow-hidden group px-8 py-2.5 rounded-full text-white text-sm font-medium transition-all duration-300 ease-out focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 shadow-lg shadow-orange-500/20">
            <span class="absolute inset-0 bg-gradient-to-r from-orange-500 to-red-500 transition-transform duration-300 ease-out group-hover:scale-105"></span>
            <span class="absolute -inset-px rounded-full bg-gradient-to-r from-orange-400 via-pink-400 to-purple-500 opacity-0 transition-opacity duration-500 group-hover:opacity-100"
                  :style="searchGlowStyle"></span>
            <span class="relative z-10 flex items-center justify-center">
              <el-icon class="mr-1"><Search /></el-icon>
              <span>搜索</span>
            </span>
          </button>

          <!-- 重置按钮 -->
          <button @click="resetFilters"
            class="px-6 py-2.5 rounded-full text-gray-600 text-sm font-medium border border-gray-200 hover:bg-gray-50 transition-all duration-200">
            重置
          </button>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="flex-1 bg-white/60 backdrop-blur-md border border-white/50 p-8 rounded-[2.5rem] shadow-soft-blue animate-fade-in-up" style="animation-delay: 200ms;">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-xl font-semibold text-gray-800">
            全部记录
            <span class="ml-2 text-sm font-normal text-gray-400">共 {{ total }} 条</span>
          </h3>
          <button @click="fetchRecords" class="text-sm text-orange-600 hover:text-orange-700 font-medium flex items-center gap-1 transition-colors">
            <el-icon class="animate-spin-on-hover"><Refresh /></el-icon>
            刷新
          </button>
        </div>

        <el-table
          :data="records"
          v-loading="loading"
          style="width: 100%"
          class="custom-record-table"
          :row-class-name="recordRowClassName"
          empty-text="暂无识别记录"
          stripe
          align="center"
        >
          <el-table-column prop="id" label="ID" width="80" align="center">
            <template #default="scope">
              <span class="text-gray-400 font-mono text-xs">#{{ scope.row.id }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="type" label="识别类型" width="160" align="center">
            <template #default="scope">
              <span :class="['px-3 py-1 rounded-full text-xs font-medium', getTypeTagClass(scope.row.type)]">
                {{ getTypeLabel(scope.row.type) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="recognitionTime" label="识别时间" width="200" align="center">
            <template #default="scope">
              <div class="flex items-center justify-center text-gray-600 text-sm">
                <el-icon class="mr-1.5 text-gray-400"><Clock /></el-icon>
                {{ formatTime(scope.row.recognitionTime) }}
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="operatorName" label="操作员" width="160" align="center">
            <template #default="scope">
              <div class="flex items-center justify-center">
                <div class="w-6 h-6 rounded-full bg-orange-100 flex items-center justify-center mr-2">
                  <el-icon class="text-orange-500 w-3.5 h-3.5"><User /></el-icon>
                </div>
                <span class="text-gray-800 font-medium text-sm">{{ scope.row.operatorName || '-' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="recognitionResult" label="识别结果" align="center">
            <template #default="scope">
              <div class="flex items-center justify-center">
                <div :class="['w-2 h-2 rounded-full mr-2 shrink-0', scope.row.status === 'done' ? 'bg-emerald-500' : 'bg-red-500']"></div>
                <span v-if="scope.row.status === 'done'" class="font-medium text-sm text-emerald-700">
                  {{ scope.row.recognitionResult || '-' }}
                  <span v-if="scope.row.confidence != null" class="ml-1 text-orange-600">
                    {{ (scope.row.confidence * 100).toFixed(1) }}%
                  </span>
                </span>
                <span v-else class="font-medium text-sm text-red-600">识别失败</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="scope">
              <div class="flex items-center justify-center gap-2">
                <el-button 
                  type="primary" 
                  size="small" 
                  circle 
                  plain
                  :icon="Document"
                  @click="goReport(scope.row)"
                  title="查看报告"
                />
                <el-button 
                  type="danger" 
                  size="small" 
                  circle 
                  plain
                  :icon="Delete"
                  @click="handleDelete(scope.row)"
                  title="删除记录"
                />
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="mt-6 flex justify-center">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Refresh, Clock, User, Delete, Document } from '@element-plus/icons-vue';
import { getRecognitionRecords, deleteRecognitionRecord } from '@/services/modules/recognition.js';
import { ElMessageBox, ElMessage } from 'element-plus';

const router = useRouter();

// 筛选条件
const filters = ref({
  recordId: '',
  type: '',
  dateRange: null,
});

// 表格数据
const records = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 搜索按钮光晕
const searchGlowStyle = ref({});
const handleSearchMouseMove = (e) => {
  const rect = e.currentTarget.getBoundingClientRect();
  const x = e.clientX - rect.left;
  const y = e.clientY - rect.top;
  searchGlowStyle.value = {
    background: `radial-gradient(circle at ${x}px ${y}px, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 70%)`
  };
};
const handleSearchMouseLeave = () => { searchGlowStyle.value = {}; };

// 类型标签样式
const getTypeTagClass = (type) => {
  const map = {
    human: 'bg-orange-100 text-orange-700',
    non_human: 'bg-emerald-100 text-emerald-700',
  };
  return map[type] || 'bg-gray-100 text-gray-700';
};

const getTypeLabel = (type) => {
  const map = {
    human: '人类识别',
    non_human: '非人类识别',
  };
  return map[type] || type;
};

// 格式化时间
const formatTime = (isoStr) => {
  if (!isoStr) return '-';
  const d = new Date(isoStr);
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  });
};

// 表格行样式
const recordRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 0 ? '' : 'bg-orange-50/30';
};

// 获取记录
const fetchRecords = async () => {
  loading.value = true;
  try {
    const params = {};
    if (filters.value.recordId) params.recordId = filters.value.recordId;
    if (filters.value.type) params.type = filters.value.type;
    if (filters.value.dateRange && filters.value.dateRange.length === 2) {
      params.startTime = filters.value.dateRange[0];
      // Fix: set end-of-day so records created during the end day are included
      params.endTime = filters.value.dateRange[1].replace('T00:00:00', 'T23:59:59');
    }

    const res = await getRecognitionRecords(params);
    if (res.code === 200 && res.data) {
      records.value = res.data;
      total.value = res.data.length;
    }
  } catch (error) {
    // API 未就绪时使用 mock 数据
    records.value = generateMockData();
    total.value = records.value.length;
  } finally {
    loading.value = false;
  }
};

// 重置筛选
const resetFilters = () => {
  filters.value = { recordId: '', type: '', dateRange: null };
  fetchRecords();
};

// 分页
const handleSizeChange = () => { fetchRecords(); };
const handlePageChange = () => { fetchRecords(); };

// Mock 数据（与真实 API 字段保持一致）
const generateMockData = () => {
  const types = ['human', 'non_human'];
  const operators = ['admin', 'operator01', 'operator02', 'supervisor'];
  const data = [];

  for (let i = 1; i <= 18; i++) {
    const date = new Date(2026, 2, 21 - Math.floor(i / 3), 8 + (i % 12), i * 3, 0);
    const succeeded = i % 5 !== 0;
    data.push({
      id: i,
      type: types[i % types.length],
      recognitionTime: date.toISOString().replace('Z', ''),
      operatorName: operators[i % operators.length],
      recognitionResult: succeeded ? String(1000 + i) : null,
      confidence: succeeded ? (0.7 + (i % 30) / 100) : null,
      status: succeeded ? 'done' : 'failed',
    });
  }
  return data;
};

// 跳转到报告详情
const goReport = (row) => {
  router.push(`/report/${row.id}`);
};

// 删除记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除记录 #${row.id} 吗？此操作不可撤销。`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
      }
    );
    
    loading.value = true;
    const res = await deleteRecognitionRecord(row.id);
    if (res.code === 200) {
      ElMessage.success('记录删除成功');
      // 如果后端没挂，这里应该重新加载。如果由于是mock模式或者后端问题抛错，就在catch里提示。
      await fetchRecords(); 
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete record:', error);
      // 如果报错是因为接口不存在（如本地开发中），则提示。
      ElMessage.error(error.message || '删除失败');
      
      // 这里的逻辑可以改为：即便API报错，但在演示环境下我们也从数组里移除（演示需要）。
      // records.value = records.value.filter(r => r.id !== row.id); 
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchRecords();
});
</script>

<style scoped>
/* 自定义表格样式，覆盖 Element Plus 默认 */
:deep(.custom-record-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-border-color: rgba(0, 0, 0, 0.04);
  --el-table-row-hover-bg-color: rgba(249, 115, 22, 0.04);
  border-radius: 1rem;
  overflow: hidden;
}

:deep(.custom-record-table .el-table__header th) {
  font-weight: 600;
  color: #6b7280;
  font-size: 1.6rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 14px 0;
  border-bottom: 2px solid rgba(249, 115, 22, 0.15);
  text-align: center;
}

:deep(.custom-record-table .el-table__body td) {
  padding: 14px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);
}

/* 分页样式覆盖 */
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 0.75rem;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #f97316, #ef4444);
  color: white;
}
</style>
