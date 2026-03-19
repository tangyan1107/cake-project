<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/report'
import {
  TrendCharts,
  User,
  ShoppingCart,
  Trophy
} from '@element-plus/icons-vue'

// 日期范围
const dateRange = ref([])

// 当前选中的时间标签
const activeTimeTab = ref('近7日')

// 时间标签选项
const timeTabs = ['昨日', '近7日', '近30日', '本周', '本月']

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
  
  if (!turnoverChart) {
    turnoverChart = echarts.init(chartDom)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#409EFF',
          type: 'dashed'
        }
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
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: turnoverData.value.dateList,
      axisLine: {
        lineStyle: {
          color: '#DCDFE6'
        }
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
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
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        sampling: 'average',
        itemStyle: {
          color: '#409EFF',
          borderWidth: 2,
          borderColor: '#fff'
        },
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        data: turnoverData.value.turnoverList
      }
    ]
  }
  turnoverChart.setOption(option, true)
}

// 初始化用户统计图表
const initUserChart = () => {
  const chartDom = document.getElementById('userChart')
  if (!chartDom) return
  
  if (!userChart) {
    userChart = echarts.init(chartDom)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#409EFF',
          type: 'dashed'
        }
      }
    },
    legend: {
      data: ['all', 'new'],
      bottom: 0,
      icon: 'roundRect',
      itemWidth: 20,
      itemHeight: 3
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: userData.value.dateList,
      axisLine: {
        lineStyle: {
          color: '#DCDFE6'
        }
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    series: [
      {
        name: 'all',
        type: 'line',
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#409EFF',
          borderWidth: 2,
          borderColor: '#fff'
        },
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        data: userData.value.totalUserList
      },
      {
        name: 'new',
        type: 'line',
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#67C23A',
          borderWidth: 2,
          borderColor: '#fff'
        },
        lineStyle: {
          color: '#67C23A',
          width: 2
        },
        data: userData.value.newUserList
      }
    ]
  }
  userChart.setOption(option, true)
}

// 初始化订单统计图表
const initOrderChart = () => {
  const chartDom = document.getElementById('orderChart')
  if (!chartDom) return
  
  if (!orderChart) {
    orderChart = echarts.init(chartDom)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#409EFF',
          type: 'dashed'
        }
      }
    },
    legend: {
      data: ['all', 'order'],
      bottom: 0,
      icon: 'roundRect',
      itemWidth: 20,
      itemHeight: 3
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: orderData.value.dateList,
      axisLine: {
        lineStyle: {
          color: '#DCDFE6'
        }
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    series: [
      {
        name: 'all',
        type: 'line',
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#409EFF',
          borderWidth: 2,
          borderColor: '#fff'
        },
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        data: orderData.value.orderCountList
      },
      {
        name: 'order',
        type: 'line',
        smooth: false,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#67C23A',
          borderWidth: 2,
          borderColor: '#fff'
        },
        lineStyle: {
          color: '#67C23A',
          width: 2
        },
        data: orderData.value.validOrderCountList
      }
    ]
  }
  orderChart.setOption(option, true)
}

// 初始化销量TOP10图表
const initTop10Chart = () => {
  const chartDom = document.getElementById('top10Chart')
  if (!chartDom) return
  
  if (!top10Chart) {
    top10Chart = echarts.init(chartDom)
  }
  
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
      right: '8%',
      top: '5%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      min: 0,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#606266',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: '#EBEEF5'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: top10Data.value.nameList,
      axisLine: {
        lineStyle: {
          color: '#DCDFE6'
        }
      },
      axisLabel: {
        color: '#606266',
        fontSize: 11,
        interval: 0
      },
      axisTick: {
        show: false
      }
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: '#409EFF',
          borderRadius: [0, 4, 4, 0]
        },
        data: top10Data.value.numberList,
        label: {
          show: true,
          position: 'right',
          color: '#606266',
          fontSize: 11
        }
      }
    ]
  }
  top10Chart.setOption(option, true)
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
      // 解析逗号分隔的字符串为数组
      const dateList = res.data.dateList ? res.data.dateList.split(',') : []
      const turnoverList = res.data.turnoverList ? res.data.turnoverList.split(',').map(v => parseFloat(v) || 0) : []
      
      turnoverData.value = {
        dateList,
        turnoverList
      }
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
      // 解析逗号分隔的字符串为数组
      const dateList = res.data.dateList ? res.data.dateList.split(',') : []
      const newUserList = res.data.newUserList ? res.data.newUserList.split(',').map(v => parseInt(v) || 0) : []
      const totalUserList = res.data.totalUserList ? res.data.totalUserList.split(',').map(v => parseInt(v) || 0) : []
      
      userData.value = {
        dateList,
        newUserList,
        totalUserList
      }
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
      // 解析逗号分隔的字符串为数组
      const dateList = res.data.dateList ? res.data.dateList.split(',') : []
      const orderCountList = res.data.orderCountList ? res.data.orderCountList.split(',').map(v => parseInt(v) || 0) : []
      const validOrderCountList = res.data.validOrderCountList ? res.data.validOrderCountList.split(',').map(v => parseInt(v) || 0) : []
      
      orderData.value = {
        dateList,
        orderCountList,
        validOrderCountList,
        totalOrderCount: res.data.totalOrderCount || 0,
        validOrderCount: res.data.validOrderCount || 0,
        orderCompletionRate: res.data.orderCompletionRate || 0
      }
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
      // 解析逗号分隔的字符串为数组
      const nameList = res.data.nameList ? res.data.nameList.split(',') : []
      const numberList = res.data.numberList ? res.data.numberList.split(',').map(v => parseInt(v) || 0) : []
      
      // 将数据反转，让销量高的排在上方
      top10Data.value = {
        nameList: nameList.reverse(),
        numberList: numberList.reverse()
      }
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

// 是否为自定义日期范围
const isCustomDateRange = ref(false)

// 搜索 - 仅查询自定义日期范围，如果日期不匹配任何标签则取消标签选中状态
const handleSearch = () => {
  // 检查当前日期范围是否匹配某个标签
  const currentBegin = dateRange.value?.[0]
  const currentEnd = dateRange.value?.[1]
  
  if (currentBegin && currentEnd) {
    let matchedTab = null
    for (const tab of timeTabs) {
      const [tabBegin, tabEnd] = getDateRangeByTab(tab)
      if (currentBegin === tabBegin && currentEnd === tabEnd) {
        matchedTab = tab
        break
      }
    }
    
    // 如果没有匹配的标签，清空选中状态并标记为自定义日期
    if (!matchedTab) {
      activeTimeTab.value = null
      isCustomDateRange.value = true
    } else {
      activeTimeTab.value = matchedTab
      isCustomDateRange.value = false
    }
  }
  
  loadAllData()
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

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取日期范围根据标签
const getDateRangeByTab = (tab) => {
  const end = new Date()
  const start = new Date()
  
  switch (tab) {
    case '昨日':
      start.setDate(start.getDate() - 1)
      end.setDate(end.getDate() - 1)
      return [formatDate(start), formatDate(end)]
    case '近7日':
      start.setDate(start.getDate() - 6)
      return [formatDate(start), formatDate(end)]
    case '近30日':
      start.setDate(start.getDate() - 29)
      return [formatDate(start), formatDate(end)]
    case '本周':
      const dayOfWeek = start.getDay() || 7
      start.setDate(start.getDate() - dayOfWeek + 1)
      return [formatDate(start), formatDate(end)]
    case '本月':
      start.setDate(1)
      return [formatDate(start), formatDate(end)]
    default:
      start.setDate(start.getDate() - 6)
      return [formatDate(start), formatDate(end)]
  }
}

// 切换时间标签
const handleTimeTabChange = (tab) => {
  activeTimeTab.value = tab
  isCustomDateRange.value = false
  dateRange.value = getDateRangeByTab(tab)
  loadAllData()
}

// 设置默认日期范围（最近7天）
const setDefaultDateRange = () => {
  dateRange.value = getDateRangeByTab('近7日')
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
            :class="{ 'custom-date-active': isCustomDateRange }"
          />
          <el-button type="primary" :icon="TrendCharts" @click="handleSearch" style="margin-left: 12px;">
            查询数据
          </el-button>
        </div>
        <div class="time-tabs">
          <span
            v-for="tab in timeTabs"
            :key="tab"
            :class="['time-tab', { active: activeTimeTab === tab }]"
            @click="handleTimeTabChange(tab)"
          >
            {{ tab }}
          </span>
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
  flex-wrap: wrap;
  gap: 16px;
}

.search-form .search-item {
  flex-shrink: 0;
}

.search-form .time-tabs {
  flex: 1;
  justify-content: flex-end;
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

.time-tabs {
  display: flex;
  gap: 8px;
}

.time-tab {
  padding: 4px 12px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.time-tab:hover {
  color: #409EFF;
  background-color: #ECF5FF;
}

.time-tab.active {
  color: #fff;
  background-color: #409EFF;
}

/* 自定义日期范围时日期选择器的样式 */
:deep(.custom-date-active .el-input__wrapper) {
  background-color: #fdf6ec;
  box-shadow: 0 0 0 1px #e6a23c inset;
}

:deep(.custom-date-active .el-input__inner) {
  color: #e6a23c;
  font-weight: 500;
}

:deep(.custom-date-active .el-range-separator) {
  color: #e6a23c;
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
  overflow: hidden;
}

.chart-card {
  height: 420px;
  overflow: hidden !important;
}

.chart-card :deep(.el-card__body) {
  overflow: hidden !important;
}

/* 隐藏所有滚动条 */
.chart-card ::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
}

.chart-card {
  -ms-overflow-style: none !important;
  scrollbar-width: none !important;
}

.chart-container {
  width: 100%;
  height: 320px;
  overflow: hidden;
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
