<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/report'
import {
  TrendCharts,
  User,
  ShoppingCart,
  Trophy,
  Download
} from '@element-plus/icons-vue'

// 日期范围
const dateRange = ref([])

// 加载状态
const loading = ref(false)

// 图表实例
let turnoverChart = null
let userChart = null
let orderChart = null
let top10Chart = null

// 统计数据
const turnoverData = ref({
  dateList: [],
  turnoverList: []
})

const userData = ref({
  dateList: [],
  newUserList: [],
  totalUserList: []
})

const orderData = ref({
  dateList: [],
  orderCountList: [],
  validOrderCountList: [],
  totalOrderCount: 0,
  validOrderCount: 0,
  orderCompletionRate: 0
})

const top10Data = ref({
  nameList: [],
  numberList: []
})

// 初始化营业额统计图表
const initTurnoverChart = () => {
  const chartDom = document.getElementById('turnoverChart')
  if (!chartDom) return
  
  turnoverChart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      },
      formatter: function(params) {
        return params[0].name + '<br/>' +
               params[0].marker + ' 营业额: ¥' + params[0].value.toFixed(2)
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: turnoverData.value.dateList,
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    series: [
      {
        name: '营业额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        sampling: 'average',
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        data: turnoverData.value.turnoverList
      }
    ]
  }
  turnoverChart.setOption(option)
}

// 初始化用户统计图表
const initUserChart = () => {
  const chartDom = document.getElementById('userChart')
  if (!chartDom) return
  
  userChart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['新增用户', '总用户量'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: userData.value.dateList,
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#67C23A'
        },
        data: userData.value.newUserList
      },
      {
        name: '总用户量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#409EFF'
        },
        data: userData.value.totalUserList
      }
    ]
  }
  userChart.setOption(option)
}

// 初始化订单统计图表
const initOrderChart = () => {
  const chartDom = document.getElementById('orderChart')
  if (!chartDom) return
  
  orderChart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line'
      }
    },
    legend: {
      data: ['订单总数', '有效订单'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: orderData.value.dateList,
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    series: [
      {
        name: '订单总数',
        type: 'line',
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          width: 2
        },
        data: orderData.value.orderCountList
      },
      {
        name: '有效订单',
        type: 'line',
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#67C23A'
        },
        lineStyle: {
          width: 2
        },
        data: orderData.value.validOrderCountList
      }
    ]
  }
  orderChart.setOption(option)
}

// 初始化销量TOP10图表
const initTop10Chart = () => {
  const chartDom = document.getElementById('top10Chart')
  if (!chartDom) return
  
  top10Chart = echarts.init(chartDom)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        return params[0].name + '<br/>' +
               params[0].marker + ' 销量: ' + params[0].value
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: top10Data.value.nameList,
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      },
      axisLabel: {
        rotate: 30,
        interval: 0,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#909399'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        barWidth: '50%',
        itemStyle: {
          color: '#409EFF',
          borderRadius: [4, 4, 0, 0]
        },
        data: top10Data.value.numberList
      }
    ]
  }
  top10Chart.setOption(option)
}

// 获取营业额统计数据
const getTurnoverData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  try {
    const params = {
      begin: dateRange.value[0],
      end: dateRange.value[1]
    }
    const res = await reportApi.getTurnoverStatistics(params)
    if (res.code === 1) {
      turnoverData.value = res.data
      nextTick(() => {
        initTurnoverChart()
      })
    }
  } catch (error) {
    console.error('获取营业额统计失败:', error)
  }
}

// 获取用户统计数据
const getUserData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  try {
    const params = {
      begin: dateRange.value[0],
      end: dateRange.value[1]
    }
    const res = await reportApi.getUserStatistics(params)
    if (res.code === 1) {
      userData.value = res.data
      nextTick(() => {
        initUserChart()
      })
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

// 获取订单统计数据
const getOrderData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  try {
    const params = {
      begin: dateRange.value[0],
      end: dateRange.value[1]
    }
    const res = await reportApi.getOrdersStatistics(params)
    if (res.code === 1) {
      orderData.value = res.data
      nextTick(() => {
        initOrderChart()
      })
    }
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

// 获取销量TOP10数据
const getTop10Data = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  try {
    const params = {
      begin: dateRange.value[0],
      end: dateRange.value[1]
    }
    const res = await reportApi.getTop10(params)
    if (res.code === 1) {
      top10Data.value = res.data
      nextTick(() => {
        initTop10Chart()
      })
    }
  } catch (error) {
    console.error('获取销量TOP10失败:', error)
  }
}

// 加载所有数据
const loadAllData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  
  loading.value = true
  await Promise.all([
    getTurnoverData(),
    getUserData(),
    getOrderData(),
    getTop10Data()
  ])
  loading.value = false
}

// 搜索
const handleSearch = () => {
  loadAllData()
}

// 导出Excel
const handleExport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  
  try {
    const params = {
      begin: dateRange.value[0],
      end: dateRange.value[1]
    }
    const res = await reportApi.exportExcel(params)
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `运营数据报表_${dateRange.value[0]}_${dateRange.value[1]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出失败:', error)
  }
}

// 格式化百分比
const formatPercent = (value) => {
  if (value === null || value === undefined) return '0%'
  return (value * 100).toFixed(1) + '%'
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  turnoverChart?.resize()
  userChart?.resize()
  orderChart?.resize()
  top10Chart?.resize()
}

// 设置默认日期范围（最近7天）
const setDefaultDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 6)
  
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  dateRange.value = [formatDate(start), formatDate(end)]
}

onMounted(() => {
  setDefaultDateRange()
  loadAllData()
  window.addEventListener('resize', handleResize)
})
</script>

<template>
  <div class="statistics-container" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据统计</h2>
      <p>查看店铺运营数据报表</p>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <div class="search-form">
        <div class="search-item">
          <span class="search-label">日期范围：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 320px"
          />
        </div>
        <div class="search-actions">
          <el-button type="primary" :icon="TrendCharts" @click="handleSearch">
            查询数据
          </el-button>
          <el-button :icon="Download" @click="handleExport">
            导出报表
          </el-button>
        </div>
      </div>
    </el-card>



    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 营业额统计 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="card-title-wrapper">
              <div class="card-icon turnover">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <span class="card-title">营业额统计</span>
            </div>
          </div>
        </template>
        <div id="turnoverChart" class="chart-container"></div>
      </el-card>

      <!-- 用户统计 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="card-title-wrapper">
              <div class="card-icon user">
                <el-icon><User /></el-icon>
              </div>
              <span class="card-title">用户统计</span>
            </div>
          </div>
        </template>
        <div id="userChart" class="chart-container"></div>
      </el-card>

      <!-- 订单统计 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="card-title-wrapper">
              <div class="card-icon order">
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <span class="card-title">订单统计</span>
            </div>
          </div>
        </template>
        <div class="order-summary">
          <div class="summary-item">
            <span class="summary-label">订单总数</span>
            <span class="summary-value">{{ orderData.totalOrderCount }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">有效订单</span>
            <span class="summary-value">{{ orderData.validOrderCount }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">订单完成率</span>
            <span class="summary-value">{{ formatPercent(orderData.orderCompletionRate) }}</span>
          </div>
        </div>
        <div id="orderChart" class="chart-container" style="height: 260px;"></div>
      </el-card>

      <!-- 销量TOP10 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="card-title-wrapper">
              <div class="card-icon top10">
                <el-icon><Trophy /></el-icon>
              </div>
              <span class="card-title">销量排名TOP10</span>
            </div>
          </div>
        </template>
        <div id="top10Chart" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.statistics-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin-bottom: 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.search-item {
  display: flex;
  align-items: center;
}

.search-label {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}

.search-actions {
  display: flex;
  gap: 10px;
}

.overview-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.card-icon.turnover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.user {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.order {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.top10 {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.overview-stats {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.overview-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.overview-icon.total {
  background: linear-gradient(135deg, #409EFF 0%, #79BBFF 100%);
}

.overview-icon.valid {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.overview-icon.rate {
  background: linear-gradient(135deg, #E6A23C 0%, #F0C78A 100%);
}

.overview-info {
  display: flex;
  flex-direction: column;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  height: 420px;
}

.chart-container {
  width: 100%;
  height: 320px;
}

.order-summary {
  display: flex;
  gap: 30px;
  margin-bottom: 15px;
  padding: 0 10px;
}

.summary-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.summary-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .overview-stats {
    flex-direction: column;
    gap: 20px;
  }
  
  .search-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .search-actions .el-button {
    width: 100%;
  }
}
</style>
