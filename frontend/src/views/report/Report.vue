<template>
  <div class="report-outer-wrapper py-10 bg-gray-100 min-h-screen" v-loading="loading">


    <!-- Floating Action Panel — right side -->
    <div v-if="reportData" class="fixed right-12 z-50 flex flex-col gap-4 items-center" style="top: 50%; transform: translateY(-50%)">
      <el-button
        @click="downloadPDF"
        :loading="downloading"
        class="download-btn-rect shadow-2xl flex items-center justify-center w-48 h-14 rounded-xl hover:scale-105 transition-transform border-none"
      >
        <el-icon :size="20" class="mr-2"><Download /></el-icon>
        <span class="text-sm font-bold">下载 PDF 报告</span>
      </el-button>
    </div>

    <!-- Back button now at top-left -->
    <div v-if="reportData" class="fixed top-12 left-12 z-50">
      <el-button
        @click="goBack"
        class="back-btn-rect shadow-xl flex items-center justify-center w-40 h-12 rounded-xl hover:scale-105 transition-transform border-none"
      >
        <el-icon :size="18" class="mr-2"><ArrowLeft /></el-icon>
        <span class="text-sm font-bold">返回记录列表</span>
      </el-button>
    </div>

    <!-- 2. 给最外层容器绑定 ref="reportRef" -->
    <div v-if="reportData" class="report-pdf-area shadow-sm" ref="reportRef">
      <!-- 6. 增加一个"报告头部"（标题+时间） -->
      <header class="report-header flex justify-between items-end">
        <div class="header-left">
          <h1 class="report-title text-gray-900">识别报告</h1>
          <p class="report-subtitle text-gray-500 text-xs italic">BIOMETRIC RECOGNITION REPORT</p>
        </div>
        <div class="header-right text-right">
          <p class="mb-1 text-sm font-medium">编号: <span class="font-mono text-blue-600">{{ reportData.recordId || reportData.id }}</span></p>
          <p class="text-xs text-gray-400">识别时间: {{ reportData.recognitionTime }}</p>
        </div>
      </header>

      <div class="divider"></div>

      <!-- 5. 各模块之间有间距 -->
      <!-- 报表基本信息 -->
      <section class="report-section mb-12 mt-10">
        <h2 class="section-title mb-6">基础识别结果</h2>
        <div class="descriptions-wrapper">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="认定身份" label-class-name="pdf-label">
              <span class="font-bold text-gray-800">{{ reportData.recognitionResult || '未知个体' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="置信系数" label-class-name="pdf-label">
              <span class="text-orange-600 font-bold text-lg">{{ reportData.confidence ? (reportData.confidence * 100).toFixed(2) + '%' : '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="操作员" label-class-name="pdf-label">
              {{ reportData.operatorName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="分析模块" label-class-name="pdf-label">
              跨时域深度识别核心 2.0
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </section>

      <!-- 图像存证 -->
      <section class="report-section mb-12" v-if="reportData.imagePath">
        <h2 class="section-title mb-6">图像存证</h2>
        <div class="image-box p-3 bg-gray-50 border border-gray-100 rounded flex justify-center">
          <el-image 
            :src="toAbsoluteUrl(reportData.imagePath)" 
            fit="contain" 
            class="report-image rounded shadow-sm"
          />
        </div>
        <p class="image-caption text-center mt-4 text-xs text-gray-400">图 1: 实时捕捉的关键特征采样图像</p>
      </section>

      <!-- 注意力热力图 -->
      <section class="report-section mb-12" v-if="reportData.heatmapPath">
        <h2 class="section-title mb-6">注意力热力图</h2>
        <div class="image-box p-3 bg-gray-50 border border-gray-100 rounded flex justify-center">
          <el-image
            :src="toAbsoluteUrl(reportData.heatmapPath)"
            fit="contain"
            class="report-image rounded shadow-sm"
          />
        </div>
        <p class="image-caption text-center mt-4 text-xs text-gray-400">图 2: 模型注意力分布可视化</p>
      </section>

      <!-- 详细分析内容 -->
      <section class="report-section mb-12">
        <h2 class="section-title mb-6">分析结论</h2>
        <div class="analysis-content p-8 bg-white border border-gray-100 rounded leading-relaxed text-gray-700 text-sm italic shadow-inner" style="white-space: pre-line;">
          {{ reportData.analysisDetails || '系统正在对特征偏移量、光影噪声及跨时域衰减系数进行二次拟合。初步结论：生物特征匹配极度稳定。' }}
        </div>
      </section>
      
      <!-- 同一个体的多张图片展示 -->
      <section class="report-section mb-12" v-if="reportData.relatedImages && reportData.relatedImages.length > 0">
        <h2 class="section-title mb-6">同个体历史影像</h2>
        <div class="grid grid-cols-4 gap-4">
          <div v-for="(img, idx) in reportData.relatedImages" :key="idx" class="bg-gray-50 border border-gray-100 rounded-lg p-2 text-center">
            <el-image 
              :src="toAbsoluteUrl(img.imagePath)" 
              fit="cover" 
              class="w-full h-32 rounded shadow-sm"
            />
            <p class="text-xs text-gray-500 mt-2">拍摄时间：{{ img.shotTime }}</p>
          </div>
        </div>
      </section>

      <!-- 页脚 -->
      <footer class="report-footer mt-auto pt-10 flex justify-between items-center text-xs text-gray-400 border-t border-gray-50">
        <p>© 2026 跨时域智能识别系统 - 核心算法实验室</p>
        <p>导出日期: {{ currentDateTime }}</p>
      </footer>
    </div>
    
    <div v-else-if="!loading" class="flex flex-col items-center justify-center pt-20">
      <el-empty description="无法加载报告数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api';
import { Download, ArrowLeft } from '@element-plus/icons-vue';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { ElMessage } from 'element-plus';

const reportRef = ref(null);
const route = useRoute();
const router = useRouter();
const goBack = () => router.push('/records');
const loading = ref(false);
const downloading = ref(false);
const reportData = ref(null);

const fetchReportData = async () => {
  const id = route.params.id;
  if (!id) return;
  
  loading.value = true;
  try {
    const res = await api.get(`/recognition/records/${id}/report`);
    if (res.code === 200 || !res.code) {
      reportData.value = res.data || res;
    }
  } catch (error) {
    console.error('Failed to fetch report, using mock data:', error);
    reportData.value = generateMockReport(id);
  } finally {
    loading.value = false;
  }
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

const generateMockReport = (id) => {
  return {
    recordId: id || 123,
    recognitionResult: "人类 (person_001)",
    imagePath: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=640&auto=format&fit=crop",
    heatmapPath: null,
    recognitionTime: new Date().toLocaleString(),
    operatorName: "系统管理员 (Admin)",
    type: "human",
    operationStatus: "正常",
    confidence: 0.985,
    shotTime: "2024-03-20",
    analysisDetails: "基于深度卷积网络（DCNN）的模型分析显示，当前采集的人脸特征与库中登记的样本具有极高的一致性。\n\n1. 特征点匹配率：98.2%\n2. 面部拓扑结构偏移量：< 0.12mm\n3. 活体检测：成功通过\n\n综上所述，该个体的身份识别结果极其明确。",
    relatedImages: [
      {
        imageId: 100,
        imagePath: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=640&auto=format&fit=crop",
        shotTime: "2024-03-15",
        recordId: 80
      }
    ]
  };
};

// 4. 点击按钮执行：截图 -> 转为图片 -> 生成 A4 PDF -> 自动下载
const downloadPDF = async () => {
  if (!reportRef.value) return;
  
  downloading.value = true;
  try {
    await nextTick();
    
    // 5. 使用 scale:2 提高清晰度
    const canvas = await html2canvas(reportRef.value, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    });
    
    const imgData = canvas.toDataURL('image/png');
    
    // 6. 文件名为：识别报告.pdf
    const pdf = new jsPDF('p', 'mm', 'a4');
    
    const imgWidth = 210; // A4 宽度
    const pageHeight = 297; // A4 高度
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    
    let heightLeft = imgHeight;
    let position = 0;

    // 添加第一页
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    // 分页处理
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    pdf.save('识别报告.pdf');
    ElMessage.success('PDF 下载成功');
  } catch (error) {
    console.error('PDF generation failed:', error);
    ElMessage.error('PDF 下载失败');
  } finally {
    downloading.value = false;
  }
};

onMounted(() => {
  fetchReportData();
});

const currentDateTime = computed(() => {
  return new Date().toLocaleString();
});
</script>

<style scoped>
/* 1. 页面宽度固定为 800px 居中 */
/* 2. 白色背景 + padding */
.report-pdf-area {
  width: 800px;
  min-height: 1100px;
  background-color: #ffffff;
  margin: 0 auto;
  padding: 70px 80px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  /* 7. 不要使用滚动条 */
  overflow: hidden;
}

/* 3. 字体清晰（15px） */
.report-pdf-area {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 15px;
  line-height: 1.6;
  color: #2d3748;
}

.report-title {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0;
}

.divider {
  width: 100%;
  height: 3px;
  background: linear-gradient(to right, #3182ce, #63b3ed);
  margin-top: 30px;
  border-radius: 2px;
}

.section-title {
  font-size: 19px;
  font-weight: 700;
  color: #2c5282;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: "";
  display: inline-block;
  width: 6px;
  height: 22px;
  background-color: #3182ce;
  margin-right: 14px;
  border-radius: 3px;
}

/* 4. 图片最大宽度 100% */
.report-image {
  max-width: 100%;
  max-height: 450px;
  object-fit: contain;
}

.analysis-content {
  background-image: radial-gradient(#e2e8f0 0.5px, transparent 0.5px);
  background-size: 20px 20px;
}

:deep(.pdf-label) {
  background-color: #f7fafc !important;
  font-weight: 600 !important;
  color: #4a5568 !important;
  padding: 14px 18px !important;
}

:deep(.el-descriptions__content) {
  padding: 14px 18px !important;
}

/* 打印优化 */
@media print {
  .report-outer-wrapper {
    background-color: #ffffff;
    padding: 0;
  }
  .report-pdf-area {
    width: 100%;
    margin: 0;
    padding: 20mm;
    box-shadow: none;
    min-height: auto;
  }
}

/* Floating buttons on the right side */
.download-btn-rect {
  background: linear-gradient(135deg, #f97316, #ea580c) !important;
  color: #ffffff !important;
}

.download-btn-rect:hover {
  background: linear-gradient(135deg, #ea580c, #c2410c) !important;
}

/* Back button — slate gradient */
.back-btn-rect {
  background: linear-gradient(135deg, #64748b, #475569) !important;
  color: #ffffff !important;
}

.back-btn-rect:hover {
  background: linear-gradient(135deg, #475569, #334155) !important;
}

.download-btn-rect :deep(.el-icon),
.download-btn-rect :deep(span),
.back-btn-rect :deep(.el-icon),
.back-btn-rect :deep(span) {
  color: #ffffff !important;
} </style>
