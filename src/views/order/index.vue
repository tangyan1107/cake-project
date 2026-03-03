<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/order.js'
import { Search, Refresh, View, Clock, CircleCheck, Van, Select } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = ref({
  number: '',
  phone: '',
  status: '',
  beginTime: '',
  endTime: ''
})

// 订单状态选项
const statusOptions = [
  { value: 1, label: '待付款' },
  { value: 2, label: '待接单' },
  { value: 3, label: '已接单' },
  { value: 4, label: '待派送' },
  { value: 5, label: '已派送' },
  { value: 6, label: '已完成' },
  { value: 7, label: '已取消' },
  { value: 8, label: '退款' }
]

// 订单统计数据
const statistics = ref({
  toBeConfirmed: 0,
  confirmed: 0,
  deliveryInProgress: 0,
  completed: 0,
  cancelled: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页参数
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

// 订单详情侧边栏
const detailDrawer = ref(false)
const currentOrder = ref(null)

// 打开订单详情
const handleViewDetail = (row) => {
  currentOrder.value = row
  detailDrawer.value = true
}

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      number: searchForm.value.number || undefined,
      phone: searchForm.value.phone || undefined,
      status: searchForm.value.status || undefined,
      beginTime: searchForm.value.beginTime || undefined,
      endTime: searchForm.value.endTime || undefined
    }
    const res = await orderApi.getOrderList(params)
    if (res.code === 1) {
      tableData.value = res.data.records
      pagination.value.total = res.data.total
    } else {
      ElMessage.error(res.msg || '获取订单列表失败')
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取订单统计
const fetchStatistics = async () => {
  try {
    const res = await orderApi.getOrderStatistics()
    if (res.code === 1) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  fetchOrderList()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    number: '',
    phone: '',
    status: '',
    beginTime: '',
    endTime: ''
  }
  pagination.value.page = 1
  fetchOrderList()
}

// 状态标签切换
const handleStatusChange = (status) => {
  searchForm.value.status = status
  pagination.value.page = 1
  fetchOrderList()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.page = page
  fetchOrderList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  fetchOrderList()
}

// 格式化订单状态
const formatStatus = (status) => {
  const statusMap = {
    1: '待付款',
    2: '待接单',
    3: '已接单',
    4: '待派送',
    5: '已派送',
    6: '已完成',
    7: '已取消',
    8: '退款'
  }
  return statusMap[status] || '-'
}

// 获取订单状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'primary',
    3: 'primary',
    4: 'info',
    5: 'info',
    6: 'success',
    7: 'danger',
    8: 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化支付方式
const formatPayMethod = (method) => {
  const methodMap = {
    1: '微信支付',
    2: '支付宝',
    3: '线下支付'
  }
  return methodMap[method] || '-'
}

// 格式化金额
const formatAmount = (amount) => {
  return `¥${amount?.toFixed(2) || '0.00'}`
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return datetime
}

onMounted(() => {
  fetchOrderList()
  fetchStatistics()
})
</script>

<template>
  <div class="order-container">
    <!-- 统计卡片 -->
    <div class="statistics-row">
      <div class="stat-card waiting">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.toBeConfirmed }}</div>
          <div class="stat-label">待接单</div>
        </div>
      </div>
      <div class="stat-card confirmed">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.confirmed }}</div>
          <div class="stat-label">已接单</div>
        </div>
      </div>
      <div class="stat-card delivering">
        <div class="stat-icon">
          <el-icon><Van /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.deliveryInProgress }}</div>
          <div class="stat-label">派送中</div>
        </div>
      </div>
      <div class="stat-card completed">
        <div class="stat-icon">
          <el-icon><Select /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ statistics.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <div
        class="status-tab"
        :class="{ active: searchForm.status === '' }"
        @click="handleStatusChange('')"
      >
        全部订单
      </div>
      <div
        class="status-tab"
        :class="{ active: searchForm.status === 2 }"
        @click="handleStatusChange(2)"
      >
        待接单
      </div>
      <div
        class="status-tab"
        :class="{ active: searchForm.status === 4 }"
        @click="handleStatusChange(4)"
      >
        待派送
      </div>
      <div
        class="status-tab"
        :class="{ active: searchForm.status === 5 }"
        @click="handleStatusChange(5)"
      >
        派送中
      </div>
      <div
        class="status-tab"
        :class="{ active: searchForm.status === 6 }"
        @click="handleStatusChange(6)"
      >
        已完成
      </div>
      <div
        class="status-tab"
        :class="{ active: searchForm.status === 7 }"
        @click="handleStatusChange(7)"
      >
        已取消
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form inline class="search-form">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.number"
            placeholder="请输入订单号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.beginTime"
            type="datetime"
            placeholder="开始时间"
            style="width: 200px"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
          <span style="margin: 0 10px">-</span>
          <el-date-picker
            v-model="searchForm.endTime"
            type="datetime"
            placeholder="结束时间"
            style="width: 200px"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="hover" v-loading="loading">
      <el-table
        :data="tableData"
        border
        style="width: 100%"
        class="order-table"
      >
        <el-table-column prop="number" label="订单号" align="center" min-width="160" />
        <el-table-column label="订单状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" align="center" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">
              {{ formatAmount(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" align="center" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.orderTime) }}
          </template>
        </el-table-column>
        <el-table-column label="支付方式" align="center" width="100">
          <template #default="{ row }">
            {{ formatPayMethod(row.payMethod) }}
          </template>
        </el-table-column>
        <el-table-column label="联系人" align="center" width="90">
          <template #default="{ row }">
            {{ row.consignee || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="手机号" align="center" min-width="120">
          <template #default="{ row }">
            {{ row.phone }}
          </template>
        </el-table-column>
        <el-table-column label="地址" align="center" min-width="200">
          <template #default="{ row }">
            {{ row.address }}
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center" min-width="140">
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 订单详情侧边栏 -->
    <el-drawer
      v-model="detailDrawer"
      title="订单详情"
      direction="rtl"
      size="50%"
    >
      <div class="order-detail" v-if="currentOrder">
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">订单号：</span>
              <span class="value">{{ currentOrder.number }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(currentOrder.status)">
                {{ formatStatus(currentOrder.status) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">下单时间：</span>
              <span class="value">{{ currentOrder.orderTime }}</span>
            </div>
            <div class="detail-item">
              <span class="label">结账时间：</span>
              <span class="value">{{ currentOrder.checkoutTime || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">支付方式：</span>
              <span class="value">{{ formatPayMethod(currentOrder.payMethod) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">支付状态：</span>
              <el-tag :type="currentOrder.payStatus === 1 ? 'success' : 'warning'">
                {{ currentOrder.payStatus === 1 ? '已支付' : '未支付' }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">订单金额：</span>
              <span class="value amount">{{ formatAmount(currentOrder.amount) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">包装费：</span>
              <span class="value">¥{{ currentOrder.packAmount?.toFixed(2) || '0.00' }}</span>
            </div>
          </div>
        </div>

        <!-- 配送信息 -->
        <div class="detail-section">
          <div class="section-title">配送信息</div>
          <div class="detail-grid">
            <div class="detail-item full-width">
              <span class="label">收货地址：</span>
              <span class="value">{{ currentOrder.address }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系人：</span>
              <span class="value">{{ currentOrder.consignee || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">手机号：</span>
              <span class="value">{{ currentOrder.phone }}</span>
            </div>
            <div class="detail-item">
              <span class="label">预计送达：</span>
              <span class="value">{{ currentOrder.estimatedDeliveryTime || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">配送状态：</span>
              <el-tag :type="currentOrder.deliveryStatus === 1 ? 'success' : 'info'">
                {{ currentOrder.deliveryStatus === 1 ? '已送达' : '配送中' }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">送达时间：</span>
              <span class="value">{{ currentOrder.deliveryTime || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 餐具信息 -->
        <div class="detail-section">
          <div class="section-title">餐具信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">餐具数量：</span>
              <span class="value">{{ currentOrder.tablewareNumber || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="label">餐具状态：</span>
              <el-tag :type="currentOrder.tablewareStatus === 1 ? 'success' : 'info'">
                {{ currentOrder.tablewareStatus === 1 ? '已准备' : '待准备' }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 订单备注 -->
        <div class="detail-section" v-if="currentOrder.remark">
          <div class="section-title">订单备注</div>
          <div class="remark-content">{{ currentOrder.remark }}</div>
        </div>

        <!-- 订单菜品详情 -->
        <div class="detail-section">
          <div class="section-title">菜品详情</div>
          <div class="dishes-list">
            <div class="dish-item" v-if="currentOrder.orderDishes">
              {{ currentOrder.orderDishes }}
            </div>
            <div v-else class="no-dishes">暂无菜品信息</div>
          </div>
        </div>

        <!-- 取消/退款原因 -->
        <div class="detail-section" v-if="currentOrder.cancelReason || currentOrder.rejectionReason">
          <div class="section-title">订单状态说明</div>
          <div class="detail-grid">
            <div class="detail-item full-width" v-if="currentOrder.cancelReason">
              <span class="label">取消原因：</span>
              <span class="value">{{ currentOrder.cancelReason }}</span>
            </div>
            <div class="detail-item full-width" v-if="currentOrder.rejectionReason">
              <span class="label">拒绝原因：</span>
              <span class="value">{{ currentOrder.rejectionReason }}</span>
            </div>
            <div class="detail-item full-width" v-if="currentOrder.cancelTime">
              <span class="label">取消时间：</span>
              <span class="value">{{ currentOrder.cancelTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.order-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 100px);
}

.statistics-row {
  margin-bottom: 16px;
}

/* 状态筛选标签 */
.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.status-tab {
  padding: 8px 20px;
  font-size: 14px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.status-tab:hover {
  color: #409eff;
  border-color: #409eff;
}

.status-tab.active {
  color: #fff;
  background: #409eff;
  border-color: #409eff;
}

/* 统计卡片样式 */
.statistics-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: #fff;
}

.stat-card.waiting .stat-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #F0C78A 100%);
}

.stat-card.confirmed .stat-icon {
  background: linear-gradient(135deg, #409EFF 0%, #79BBFF 100%);
}

.stat-card.delivering .stat-icon {
  background: linear-gradient(135deg, #909399 0%, #C0C4CC 100%);
}

.stat-card.completed .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.stat-info {
  flex: 1;
}

.stat-info .stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-info .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.search-form .el-form-item {
  margin-bottom: 10px;
  margin-right: 0;
}

.table-card {
  margin-bottom: 16px;
  min-height: 400px;
}

.table-card .el-table {
  font-size: 14px;
}

.table-card .el-table__header th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.table-card .el-table__row {
  height: 50px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.order-dishes-text {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  color: #409eff;
}

/* 订单详情样式 */
.order-detail {
  padding: 0 10px;
}

.detail-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item.full-width {
  grid-column: span 2;
}

.detail-item .label {
  color: #909399;
  font-size: 14px;
  min-width: 80px;
}

.detail-item .value {
  color: #303133;
  font-size: 14px;
}

.detail-item .value.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.remark-content {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.dishes-list {
  background: #fff;
  padding: 12px;
  border-radius: 4px;
}

.dish-item {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}

.no-dishes {
  color: #909399;
  font-size: 14px;
  text-align: center;
  padding: 20px;
}
</style>
