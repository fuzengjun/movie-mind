<template>
  <div ref="chartRef" class="dashboard-chart" :style="{ height }"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  option: {
    type: Object,
    default: () => ({})
  },
  height: {
    type: String,
    default: '320px'
  }
})

const chartRef = ref(null)
let chartInstance
let resizeObserver

function renderChart() {
  if (!chartRef.value) {
    return
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(props.option, true)
}

function resizeChart() {
  chartInstance?.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  window.addEventListener('resize', resizeChart)
  resizeObserver = new ResizeObserver(resizeChart)
  resizeObserver.observe(chartRef.value)
})

watch(() => props.option, () => renderChart(), { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  resizeObserver?.disconnect()
  chartInstance?.dispose()
})
</script>

<style scoped>
.dashboard-chart {
  width: 100%;
  min-width: 0;
  overflow: hidden;
}
</style>