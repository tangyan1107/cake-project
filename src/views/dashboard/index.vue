<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { employeeApi } from '@/api/employee'
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
  ArrowRight
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

// 加载所有数据
const loadData = async () => {
  loading.value = true
  await Promise.all([
    getBusinessData(),
    getOverviewOrders(),
    getOverviewDishes(),
    getOverviewSetmeals()
  ])
  loading.value = false
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
          <span class="card-title">今日运营数据</span>
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
    <div class="overview-row">
      <!-- 今日订单 -->
      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">今日订单</span>
            <el-button type="primary" link @click="goToOrders" class="view-detail-btn">
              查看订单明细
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="order-list">
          <div class="order-item">
            <span class="order-label">待接单</span>
            <span class="order-value waiting">{{ orderData.waitingOrders }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">待派送</span>
            <span class="order-value delivering">{{ orderData.deliveredOrders }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">已完成</span>
            <span class="order-value completed">{{ orderData.completedOrders }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">已取消</span>
            <span class="order-value cancelled">{{ orderData.cancelledOrders }}</span>
          </div>
          <div class="order-item">
            <span class="order-label">全部订单</span>
            <span class="order-value all">{{ orderData.allOrders }}</span>
          </div>
        </div>
      </el-card>

      <!-- 菜品总览 -->
      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">菜品总览</span>
            <el-button type="primary" link @click="goToDishes" class="view-detail-btn">
              查看菜品管理
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="product-overview">
          <div class="overview-item">
            <div class="overview-icon sold">
              <el-icon :size="28"><Food /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ dishData.sold }}</div>
              <div class="overview-label">已启售</div>
            </div>
          </div>
          <div class="overview-item">
            <div class="overview-icon discontinued">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ dishData.discontinued }}</div>
              <div class="overview-label">已停售</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 套餐总览 -->
      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">套餐总览</span>
            <el-button type="primary" link @click="goToSetmeals" class="view-detail-btn">
              查看套餐管理
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="product-overview">
          <div class="overview-item">
            <div class="overview-icon sold">
              <el-icon :size="28"><Dish /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ setmealData.sold }}</div>
              <div class="overview-label">已启售</div>
            </div>
          </div>
          <div class="overview-item">
            <div class="overview-icon discontinued">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ setmealData.discontinued }}</div>
              <div class="overview-label">已停售</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
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
  margin-bottom: 20px;
}

.dashboard-header h2 {
  margin-bottom: 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.dashboard-header p {
  color: #909399;
  font-size: 14px;
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

.overview-card {
  height: 100%;
}

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
}

@media (max-width: 768px) {
  .data-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .overview-row {
    grid-template-columns: 1fr;
  }
}
</style>
