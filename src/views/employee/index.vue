<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi } from '@/api/employee.js'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  name: ''
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

// 获取员工列表
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize,
      name: searchForm.value.name || undefined
    }
    const res = await employeeApi.getEmployeePage(params)
    if (res.code === 1) {
      tableData.value = res.data.records
      pagination.value.total = res.data.total
    } else {
      ElMessage.error(res.msg || '获取员工列表失败')
    }
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  fetchEmployeeList()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.page = page
  fetchEmployeeList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.page = 1
  fetchEmployeeList()
}

// 添加员工
const handleAdd = () => {
  router.push('/employee/add')
}

// 编辑员工
const handleEdit = (row) => {
  router.push(`/employee/edit/${row.id}`)
}

// 启用/禁用员工
const handleToggleStatus = async (row) => {
  const actionText = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  
  try {
    await ElMessageBox.confirm(
      `确定${actionText}该员工吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '返回',
        type: 'warning'
      }
    )
    
    const res = await employeeApi.updateEmployeeStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success('操作成功')
      fetchEmployeeList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 格式化性别
const formatSex = (sex) => {
  const sexMap = {
    1: '男',
    2: '女'
  }
  return sexMap[sex] || '-'
}

// 格式化状态
const formatStatus = (status) => {
  return status === 1 ? '启用' : '禁用'
}

// 获取状态标签类型
const getStatusType = (status) => {
  return status === 1 ? 'success' : 'danger'
}

onMounted(() => {
  fetchEmployeeList()
})
</script>

<template>
  <div class="employee-container">
    <h2 class="page-title">员工管理</h2>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <span class="search-label">员工姓名</span>
      <el-input
        v-model="searchForm.name"
        placeholder="请输入员工姓名"
        class="search-input"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
    
    <!-- 操作按钮区域 -->
    <div class="operation-area">
      <el-button type="primary" @click="handleAdd">+ 添加员工</el-button>
    </div>
    
    <!-- 表格区域 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      border
      style="width: 100%"
      class="employee-table"
    >
      <el-table-column prop="name" label="员工姓名" align="center" />
      <el-table-column prop="username" label="账号" align="center" />
      <el-table-column prop="phone" label="手机号" align="center" />
      <el-table-column label="账号状态" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ formatStatus(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后操作时间" align="center" />
      <el-table-column label="操作" align="center" width="200">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleEdit(row)"
          >
            修改
          </el-button>
          <el-button
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
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
  </div>
</template>

<style scoped>
.employee-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.search-area {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.search-label {
  font-size: 14px;
  color: #606266;
}

.search-input {
  width: 220px;
}

.operation-area {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.employee-table {
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
}
</style>
