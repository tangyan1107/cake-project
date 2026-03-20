<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api/employee'
import { orderApi } from '@/api/order'
import {
  Money,
  ShoppingCart,
  CircleCheck,
  TrendCharts,
  User,
  Food,
  Dish,
  ShoppingBag,
  Warning,
  ArrowRight,
  Clock,
  Van
} from '@element-plus/icons-vue'

const router = useRouter()

// 今日运营数据
const businessData = ref({
  turnover: 0,
  validOrderCount: 0,
  orderCompletionRate: 0,
  unitPrice: 0,
  newUsers: 0
})

// 今日订单数据
const orderData = ref({
  waitingOrders: 0,
  deliveredOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  allOrders: 0
})

// 菜品总览
const dishData = ref({
  sold: 0,
  discontinued: 0
})

// 套餐总览
const setmealData = ref({
  sold: 0,
  discontinued: 0
})

// 加载状态
const loading = ref(false)

// 待处理订单数据（待接单和待派送）
const pendingOrders = ref([])

// 最近订单数据（用于订单信息表格）
const recentOrders = ref([])

// 获取今日运营数据
const getBusinessData = async () => {
  try {
    const res = await employeeApi.getBusinessData()
    if (res.code === 1) {
      businessData.value = res.data
    }
  } catch (error) {
    console.error('获取运营数据失败:', error)
  }
}

// 获取今日订单数据
const getOverviewOrders = async () => {
  try {
    const res = await employeeApi.getOverviewOrders()
    if (res.code === 1) {
      orderData.value = res.data
    }
  } catch (error) {
    console.error('获取订单数据失败:', error)
  }
}

// 获取菜品总览
const getOverviewDishes = async () => {
  try {
    const res = await employeeApi.getOverviewDishes()
    if (res.code === 1) {
      dishData.value = res.data
    }
  } catch (error) {
    console.error('获取菜品总览失败:', error)
  }
}

// 获取套餐总览
const getOverviewSetmeals = async () => {
  try {
    const res = await employeeApi.getOverviewSetmeals()
    if (res.code === 1) {
      setmealData.value = res.data
    }
  } catch (error) {
    console.error('获取套餐总览失败:', error)
  }
}

// 获取最近订单列表（待接单和待派送）
const getRecentOrders = async () => {
  try {
    // 获取待接单订单（status=2）
    const waitingParams = {
      page: 1,
      pageSize: 5,
      status: 2
    }
    // 获取待派送订单（status=3）
    const deliveringParams = {
      page: 1,
      pageSize: 5,
      status: 3
    }

    const [waitingRes, deliveringRes] = await Promise.all([
      orderApi.getOrderList(waitingParams),
      orderApi.getOrderList(deliveringParams)
    ])

    let orders = []
    if (waitingRes.code === 1 && waitingRes.data && waitingRes.data.records) {
      orders = orders.concat(waitingRes.data.records)
    }
    if (deliveringRes.code === 1 && deliveringRes.data && deliveringRes.data.records) {
      orders = orders.concat(deliveringRes.data.records)
    }

    // 按下单时间排序，取最新的10条
    orders.sort((a, b) => new Date(b.orderTime) - new Date(a.orderTime))
    recentOrders.value = orders.slice(0, 10)
  } catch (error) {
    console.error('获取订单列表失败:', error)
  }
}

// 接单处理
const handleConfirmOrder = async (row) => {
  try {
    const res = await orderApi.confirmOrder({ id: row.id })
    if (res.code === 1) {
      ElMessage.success('接单成功')
      getRecentOrders()
      getOverviewOrders()
    } else {
      ElMessage.error(res.msg || '接单失败')
    }
  } catch (error) {
    ElMessage.error('接单失败')
  }
}

// 拒单处理
const handleRejectionOrder = async (row) => {
  try {
    const res = await orderApi.rejectionOrder({ id: row.id, rejectionReason: '订单量较多，暂时无法接单' })
    if (res.code === 1) {
      ElMessage.success('拒单成功')
      getRecentOrders()
      getOverviewOrders()
    } else {
      ElMessage.error(res.msg || '拒单失败')
    }
  } catch (error) {
    ElMessage.error('拒单失败')
  }
}

// 派送处理
const handleDeliveryOrder = async (row) => {
  try {
    const res = await orderApi.deliveryOrder(row.id)
    if (res.code === 1) {
      ElMessage.success('派送成功')
      getRecentOrders()
      getOverviewOrders()
    } else {
      ElMessage.error(res.msg || '派送失败')
    }
  } catch (error) {
    ElMessage.error('派送失败')
  }
}

// 取消订单处理
const handleCancelOrder = async (row) => {
  try {
    const res = await orderApi.cancelOrder({ id: row.id, cancelReason: '客户电话取消' })
    if (res.code === 1) {
      ElMessage.success('取消成功')
      getRecentOrders()
      getOverviewOrders()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

// 完成订单处理
const handleCompleteOrder = async (row) => {
  try {
    const res = await orderApi.completeOrder(row.id)
    if (res.code === 1) {
      ElMessage.success('订单已完成')
      getRecentOrders()
      getOverviewOrders()
    } else {
      ElMessage.error(res.msg || '完成订单失败')
    }
  } catch (error) {
    ElMessage.error('完成订单失败')
  }
}

// 查看订单详情
const handleViewDetail = (row) => {
  router.push('/order')
}

// 状态标签映射
const getStatusTagType = (status) => {
  const typeMap = {
    2: 'warning',  // 待接单
    3: 'primary',  // 待派送
    4: 'success',  // 已完成
    5: 'info'      // 已取消
  }
  return typeMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const labelMap = {
    2: '待接单',
    3: '待派送',
    4: '已完成',
    5: '已取消'
  }
  return labelMap[status] || '未知'
}

// 加载所有数据
const loadData = async () => {
  loading.value = true
  await Promise.all([
    getBusinessData(),
    getOverviewOrders(),
    getOverviewDishes(),
    getOverviewSetmeals(),
    getRecentOrders()
  ])
  loading.value = false
}

// 获取今日日期格式化字符串
const getTodayDateString = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  return `${year}年${month}月${day}日`
}

// 格式化百分比
const formatPercent = (value) => {
  if (value === null || value === undefined) return '0%'
  return (value * 100).toFixed(1) + '%'
}

// 格式化金额
const formatMoney = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toFixed(2)
}

// 跳转到订单管理
const goToOrders = () => {
  router.push('/order')
}

// 跳转到菜品管理
const goToDishes = () => {
  router.push('/dish')
}

// 跳转到套餐管理
const goToSetmeals = () => {
  router.push('/setmeal')
}

// 跳转到数据统计
const goToStatistics = () => {
  router.push('/statistics')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 标题区域 -->
    <div class="dashboard-header">
      <h2>工作台</h2>
      <p>欢迎来到甜慕烘培坊</p>
    </div>

    <!-- 今日运营数据 -->
    <el-card class="data-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="card-title-wrapper">
            <span class="card-title">今日数据</span>
            <span class="card-date">{{ getTodayDateString() }}</span>
          </div>
          <el-button type="primary" link @click="goToStatistics" class="view-detail-btn">
            查看详情数据
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <div class="data-grid">
        <div class="data-item">
          <div class="data-icon turnover">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-label">营业额(元)</div>
            <div class="data-value">{{ formatMoney(businessData.turnover) }}</div>
          </div>
        </div>
        <div class="data-item">
          <div class="data-icon orders">
            <el-icon :size="32"><ShoppingCart /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-label">有效订单数</div>
            <div class="data-value">{{ businessData.validOrderCount }}</div>
          </div>
        </div>
        <div class="data-item">
          <div class="data-icon completion">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-label">订单完成率</div>
            <div class="data-value">{{ formatPercent(businessData.orderCompletionRate) }}</div>
          </div>
        </div>
        <div class="data-item">
          <div class="data-icon unit-price">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-label">平均客单价(元)</div>
            <div class="data-value">{{ formatMoney(businessData.unitPrice) }}</div>
          </div>
        </div>
        <div class="data-item">
          <div class="data-icon users">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-label">新增用户数</div>
            <div class="data-value">{{ businessData.newUsers }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 订单和商品概览 -->
    <div class="overview-row compact">
      <!-- 订单管理 -->
      <el-card class="overview-card compact" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单管理</span>
            <el-button type="primary" link @click="goToOrders" class="view-detail-btn">
              查看订单明细
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="order-stats-with-icon">
          <div class="order-stat-box">
            <div class="stat-icon-box waiting">
              <el-icon :size="22"><Clock /></el-icon>
            </div>
            <div class="stat-info-box">
              <div class="stat-value-text waiting">{{ orderData.waitingOrders }}</div>
              <div class="stat-label-text nowrap">待接单</div>
            </div>
          </div>
          <div class="order-stat-box">
            <div class="stat-icon-box delivering">
              <el-icon :size="22"><Van /></el-icon>
            </div>
            <div class="stat-info-box">
              <div class="stat-value-text delivering">{{ orderData.deliveredOrders }}</div>
              <div class="stat-label-text nowrap">待派送</div>
            </div>
          </div>
          <div class="order-stat-box">
            <div class="stat-icon-box completed">
              <el-icon :size="22"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info-box">
              <div class="stat-value-text completed">{{ orderData.completedOrders }}</div>
              <div class="stat-label-text nowrap">已完成</div>
            </div>
          </div>
          <div class="order-stat-box">
            <div class="stat-icon-box cancelled">
              <el-icon :size="22"><Warning /></el-icon>
            </div>
            <div class="stat-info-box">
              <div class="stat-value-text cancelled">{{ orderData.cancelledOrders }}</div>
              <div class="stat-label-text nowrap">已取消</div>
            </div>
          </div>
          <div class="order-stat-box">
            <div class="stat-icon-box all">
              <el-icon :size="22"><ShoppingBag /></el-icon>
            </div>
            <div class="stat-info-box">
              <div class="stat-value-text all">{{ orderData.allOrders }}</div>
              <div class="stat-label-text nowrap">全部订单</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 菜品总览 -->
      <el-card class="overview-card compact" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">菜品总览</span>
            <el-button type="primary" link @click="goToDishes" class="view-detail-btn">
              查看菜品管理
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="product-stats-with-icon">
          <div class="product-stat-box">
            <div class="product-icon-box sold">
              <el-icon :size="28"><Food /></el-icon>
            </div>
            <div class="product-info-box">
              <div class="product-value-text">{{ dishData.sold }}</div>
              <div class="product-label-text">已启售</div>
            </div>
          </div>
          <div class="product-stat-box">
            <div class="product-icon-box discontinued">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="product-info-box">
              <div class="product-value-text">{{ dishData.discontinued }}</div>
              <div class="product-label-text">已停售</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 套餐总览 -->
      <el-card class="overview-card compact" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">套餐总览</span>
            <el-button type="primary" link @click="goToSetmeals" class="view-detail-btn">
              查看套餐管理
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="product-stats-with-icon">
          <div class="product-stat-box">
            <div class="product-icon-box sold">
              <el-icon :size="28"><Dish /></el-icon>
            </div>
            <div class="product-info-box">
              <div class="product-value-text">{{ setmealData.sold }}</div>
              <div class="product-label-text">已启售</div>
            </div>
          </div>
          <div class="product-stat-box">
            <div class="product-icon-box discontinued">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="product-info-box">
              <div class="product-value-text">{{ setmealData.discontinued }}</div>
              <div class="product-label-text">已停售</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 订单信息 -->
    <el-card class="order-info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">订单信息</span>
          <div class="header-right">
            <div class="header-tags">
              <el-tag type="warning" size="small" effect="light" class="count-tag">
                待接单({{ orderData.waitingOrders }})
              </el-tag>
              <el-tag type="primary" size="small" effect="light" class="count-tag">
                待派送({{ orderData.deliveredOrders }})
              </el-tag>
            </div>
            <el-button type="primary" link @click="goToOrders" class="view-detail-btn">
              查看订单明细
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="recentOrders" style="width: 100%" size="small" :header-cell-style="{ background: '#f5f7fa' }">
        <el-table-column prop="number" label="订单号" width="130" align="center" />
        <el-table-column label="订单菜品" min-width="120">
          <template #default="{ row }">
            <span class="order-dishes">{{ row.orderDishes || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="100" show-overflow-tooltip />
        <el-table-column prop="estimatedDeliveryTime" label="预计送达时间" width="130" align="center" />
        <el-table-column label="实收金额" width="90" align="center">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="80" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 2" 
              link 
              type="primary" 
              size="small" 
              @click="handleConfirmOrder(row)"
            >
              接单
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              link 
              type="danger" 
              size="small" 
              @click="handleRejectionOrder(row)"
            >
              拒单
            </el-button>
            <el-button 
              v-if="row.status === 3" 
              link 
              type="primary" 
              size="small" 
              @click="handleDeliveryOrder(row)"
            >
              派送
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
}

.dashboard-header {
  margin-bottom: 12px;
}

.dashboard-header h2 {
  margin-bottom: 4px;
  color: #303133;
  font-size: 26px;
  font-weight: 600;
}

.dashboard-header p {
  color: #909399;
  font-size: 13px;
}

.data-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-date {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.view-detail-btn {
  font-size: 14px;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-detail-btn:hover {
  background-color: #f5f7fa;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.data-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.data-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
}

.data-icon.turnover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.data-icon.orders {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.data-icon.completion {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.data-icon.unit-price {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.data-icon.users {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.data-content {
  flex: 1;
}

.data-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.data-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.overview-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.overview-row.compact {
  grid-template-columns: 2fr 1fr 1fr;
  margin-bottom: 20px;
}

.overview-card {
  height: 100%;
}

.overview-card.compact {
  min-height: auto;
}

.overview-card.compact :deep(.el-card__body) {
  padding: 12px 16px;
}

/* 订单统计带图标布局 */
.order-stats-with-icon {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.order-stat-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 8px;
  background: transparent;
  border-radius: 8px;
  min-width: 0;
}

.stat-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon-box.waiting {
  background: linear-gradient(135deg, #E6A23C 0%, #F0C78A 100%);
}

.stat-icon-box.delivering {
  background: linear-gradient(135deg, #409EFF 0%, #79BBFF 100%);
}

.stat-icon-box.completed {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.stat-icon-box.cancelled {
  background: linear-gradient(135deg, #F56C6C 0%, #FAB6B6 100%);
}

.stat-icon-box.all {
  background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%);
}

.stat-info-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value-text {
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
}

.stat-value-text.waiting {
  color: #E6A23C;
}

.stat-value-text.delivering {
  color: #409EFF;
}

.stat-value-text.completed {
  color: #67C23A;
}

.stat-value-text.cancelled {
  color: #F56C6C;
}

.stat-value-text.all {
  color: #303133;
}

.stat-label-text {
  font-size: 12px;
  color: #606266;
}

.stat-label-text.nowrap {
  white-space: nowrap;
}

/* 商品统计带图标布局 */
.product-stats-with-icon {
  display: flex;
  justify-content: space-around;
  gap: 16px;
}

.product-stat-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.product-icon-box.sold {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.product-icon-box.discontinued {
  background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%);
}

.product-info-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.product-value-text {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.product-label-text {
  font-size: 12px;
  color: #909399;
}

/* 订单信息卡片 */
.order-info-card {
  margin-top: 0;
}

.order-info-card :deep(.el-card__body) {
  padding: 0;
}

.order-info-card .header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-info-card .header-tags {
  display: flex;
  gap: 8px;
}

.order-info-card .count-tag {
  font-size: 13px;
}

.order-dishes {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-info-card .amount {
  color: #f56c6c;
  font-weight: 600;
}

/* 原有样式保留 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.order-label {
  font-size: 14px;
  color: #606266;
}

.order-value {
  font-size: 20px;
  font-weight: 600;
}

.order-value.waiting {
  color: #E6A23C;
}

.order-value.delivering {
  color: #409EFF;
}

.order-value.completed {
  color: #67C23A;
}

.order-value.cancelled {
  color: #F56C6C;
}

.order-value.all {
  color: #303133;
}

.product-overview {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.overview-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: #fff;
}

.overview-icon.sold {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.overview-icon.discontinued {
  background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%);
}

.overview-info {
  text-align: center;
}

.overview-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #909399;
}

@media (max-width: 1200px) {
  .data-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .overview-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-row.compact {
    grid-template-columns: 1fr;
  }

  .order-stats-with-icon {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .data-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-row {
    grid-template-columns: 1fr;
  }

  .overview-row.compact {
    grid-template-columns: 1fr;
  }

  .order-stats-with-icon {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-stats-with-icon {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
}
</style>
