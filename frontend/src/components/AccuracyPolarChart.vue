<template>
  <div ref="chartRef" :style="{ height: height, width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  data: { type: Array, required: true },
  height: { type: String, default: '300px' }
});

const chartRef = ref(null);
let chartInstance = null;

const initChart = () => {
  if (chartInstance) chartInstance.dispose();
  chartInstance = echarts.init(chartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}%'
    },
    polar: {
      radius: ['30%', '85%'] // 留白中心，反主流
    },
    angleAxis: {
      type: 'category',
      data: props.data.map(d => d.name),
      startAngle: 90,
      splitLine: { show: false }, // 隐藏网格线
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false } // 隐藏标签，保持极简
    },
    radiusAxis: {
      min: 0,
      max: 100,
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { show: false }
    },
    series: [{
      type: 'bar', // 在极坐标下表现为弧形面积
      data: props.data.map(d => ({
        name: d.name,
        value: d.value,
        itemStyle: d.itemStyle,
        label: d.name === '指纹信息' ? { rotate: 180 } : undefined
      })),
      coordinateSystem: 'polar',
      label: {
        show: true,
        position: 'end', // Place label at the end of the arc
        formatter: '{b}: {c}%',
        color: '#6b7280', // Tailwind gray-500
        fontSize: 16,
        distance: 10,
        rotate: 0 // 禁用极坐标默认的跟随倾斜
      },
      // 动效设置
      animationEasing: 'cubicOut',
      animationDuration: 1500,
      
      // 样式设置 (反主流颜色/圆角)
      itemStyle: {
        borderRadius: [0, 10, 10, 0] // 仅弧线末端圆角
      }
    }]
  };

  chartInstance.setOption(option);
};

onMounted(() => {
  nextTick(() => {
    initChart();
    window.addEventListener('resize', () => chartInstance?.resize());
  });
});

watch(() => props.data, initChart, { deep: true });
</script>
