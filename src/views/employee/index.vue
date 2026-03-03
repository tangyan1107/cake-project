<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi } from '@/api/employee.js'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'

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

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('添加员工')
const formRef = ref(null)
const isEdit = ref(false)

// 表单数据
const formData = reactive({
  id: undefined,
  username: '',
  name: '',
  phone: '',
  sex: '1',
  idNumber: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入员工姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

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

// 重置
const handleReset = () => {
  searchForm.value.name = ''
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

// 添加员工 - 弹窗方式
const handleAdd = () => {
  dialogTitle.value = '添加员工'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑员工 - 弹窗方式
const handleEdit = async (row) => {
  dialogTitle.value = '修改员工'
  isEdit.value = true
  try {
    const res = await employeeApi.getEmployeeById(row.id)
    if (res.code === 1) {
      const data = res.data
      formData.id = data.id
      formData.username = data.username
      formData.name = data.name
      formData.phone = data.phone
      formData.sex = String(data.sex)
      formData.idNumber = data.idNumber
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取员工信息失败')
    }
  } catch (error) {
    ElMessage.error('获取员工信息失败')
  }
}

// 删除员工
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 员工删除接口 - 这里假设有deleteEmployee方法，如果没有需要添加
    if (employeeApi.deleteEmployee) {
      const res = await employeeApi.deleteEmployee(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        fetchEmployeeList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } else {
      ElMessage.info('删除功能待开发')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
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

// 保存
const handleSave = async () => {
  try {
    await formRef.value.validate()

    const api = isEdit.value ? employeeApi.updateEmployee : employeeApi.addEmployee
    const res = await api(formData)

    if (res.code === 1) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchEmployeeList()
    } else {
      ElMessage.error(res.msg || (isEdit.value ? '修改失败' : '添加失败'))
    }
  } catch (error) {
    if (error !== 'validation') {
      ElMessage.error(isEdit.value ? '修改失败' : '添加失败')
    }
  }
}

// 保存并继续添加
const handleSaveAndContinue = async () => {
  try {
    await formRef.value.validate()

    const res = await employeeApi.addEmployee(formData)

    if (res.code === 1) {
      ElMessage.success('添加成功')
      resetForm()
      fetchEmployeeList()
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    if (error !== 'validation') {
      ElMessage.error('添加失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.username = ''
  formData.name = ''
  formData.phone = ''
  formData.sex = '1'
  formData.idNumber = ''
  if (formRef.value) {
    formRef.value.resetFields()
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
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form inline>
        <el-form-item label="员工姓名">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入员工姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加员工</el-button>
    </div>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="hover" v-loading="loading">
      <el-table
        :data="tableData"
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
        <el-table-column label="操作" align="center" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
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
    </el-card>

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入账号"
            maxlength="20"
            show-word-limit
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="员工姓名" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入员工姓名"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="formData.sex">
            <el-radio value="1">男</el-radio>
            <el-radio value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input
            v-model="formData.idNumber"
            placeholder="请输入身份证号"
            maxlength="18"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
          <el-button
            v-if="!isEdit"
            type="primary"
            @click="handleSaveAndContinue"
          >
            保存并继续添加
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.employee-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.search-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
