<template>
  <div ref="chartRef" :style="{ height: height, width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  barData: { type: Array, required: true },
  lineData: { type: Array, required: true },
  categories: { type: Array, required: true },
  height: { type: String, default: '300px' }
});

const chartRef = ref(null);
let chartInstance = null;

const initChart = () => {
  if (chartInstance) chartInstance.dispose();
  chartInstance = echarts.init(chartRef.value);

  const option = {
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['识别数量', '平均准确率'],
      textStyle: { color: '#6b7280' }, // Tailwind gray-500
      bottom: 0,
      icon: 'circle',
      itemGap: 24
    },
    grid: {
      left: '0%',
      right: '0%',
      bottom: '15%',
      top: '10%',
      containLabel: true,
      borderColor: '#f3f4f6', // Tailwind gray-100
    },
    xAxis: {
      type: 'category',
      data: props.categories,
      axisLabel: { color: '#9ca3af', margin: 16 }, // Tailwind gray-400
      axisLine: { show: false }, 
      axisTick: { show: false },
      splitLine: { show: false }, 
    },
    yAxis: [
      {
        type: 'value',
        axisLabel: { color: '#9ca3af' },
        axisLine: { show: false }, 
        splitLine: { show: true, lineStyle: { color: '#f3f4f6', type: 'dashed' } }, 
      },
      {
        type: 'value',
        axisLabel: { color: '#9ca3af', formatter: '{value}%' },
        axisLine: { show: false }, 
        splitLine: { show: false }, 
        min: 0,
        max: 100,
      }
    ],
    series: [
      {
        name: '识别数量',
        type: 'bar',
        barWidth: '24px',
        data: props.barData,
        itemStyle: {
          borderRadius: [12, 12, 0, 0], // Top rounded corners
          color: function(params) {
            // Apply gradient with fading opacity towards right as requested originally, 
            // but aligned with new anti-mainstream aesthetic (orange shades)
            const startOpacity = Math.max(0.4, 1 - params.dataIndex * 0.1);
            return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: `rgba(249, 115, 22, ${startOpacity})` }, // Tailwind orange-500
              { offset: 1, color: `rgba(253, 186, 116, ${startOpacity * 0.3})` } // Tailwind orange-300
            ]);
          },
        },
        // Bouncy animation implementation
        animationEasing: 'bounceOut',
        animationDelay: function (idx) {
          return idx * 100;
        },
        animationDuration: 1500
      },
      {
        name: '平均准确率',
        type: 'line',
        yAxisIndex: 1,
        data: props.lineData,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#ea580c', // Tailwind orange-600
        },
        itemStyle: {
          color: '#ea580c',
          borderColor: '#fff',
          borderWidth: 2,
        },
        smooth: true,
        animationEasing: 'cubicOut',
        animationDuration: 2000
      },
    ],
  };
  chartInstance.setOption(option);
};

onMounted(() => {
  nextTick(() => {
    initChart();
    window.addEventListener('resize', () => chartInstance?.resize());
  });
});

watch(() => [props.barData, props.lineData, props.categories], initChart, { deep: true });
</script>
