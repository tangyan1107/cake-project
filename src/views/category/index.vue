<script setup>
import { ref, onMounted, reactive } from 'vue'
import { categoryApi } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'

// 搜索表单数据
const searchForm = reactive({
  name: '',
  type: null
})

// 分类列表数据
const categoryList = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)

// 弹窗显示状态
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  type: 1,
  sort: 0
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 选中的分类（用于批量删除）
const selectedCategories = ref([])

// 分类类型选项
const typeOptions = [
  { label: '菜品分类', value: 1 },
  { label: '套餐分类', value: 2 }
]

// 获取分类列表
const getCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined
    }
    const res = await categoryApi.getCategoryPage(params)
    if (res.code === 1) {
      categoryList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getCategoryList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.type = null
  pagination.page = 1
  getCategoryList()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  getCategoryList()
}

// 每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  getCategoryList()
}

// 新增分类
const handleAdd = () => {
  dialogTitle.value = '新增分类'
  resetForm()
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = async (row) => {
  dialogTitle.value = '编辑分类'
  try {
    const res = await categoryApi.getCategoryById(row.id)
    if (res.code === 1) {
      Object.assign(formData, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取分类详情失败:', error)
  }
}

// 删除分类
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await categoryApi.deleteCategory(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      getCategoryList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedCategories.value.length === 0) {
    ElMessage.warning('请选择要删除的分类')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedCategories.value.length} 个分类吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 逐个删除
    for (const item of selectedCategories.value) {
      await categoryApi.deleteCategory(item.id)
    }
    ElMessage.success('删除成功')
    getCategoryList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 切换状态
const handleStatusChange = async (row) => {
  // row.status 已经被 el-switch 更新为新值，需要取反得到旧状态
  const targetStatus = row.status
  const statusText = targetStatus === 1 ? '启用' : '禁用'
  try {
    const res = await categoryApi.updateCategoryStatus(targetStatus, row.id)
    if (res.code === 1) {
      ElMessage.success(`${statusText}成功`)
    } else {
      ElMessage.error(res.msg || `${statusText}失败`)
      // 恢复原状态
      row.status = targetStatus === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    // 恢复原状态
    row.status = targetStatus === 1 ? 0 : 1
  }
}

// 提交表单
const submitForm = async () => {
  try {
    const data = { ...formData }

    let res
    if (data.id) {
      // 编辑
      res = await categoryApi.updateCategory(data)
    } else {
      // 新增
      res = await categoryApi.addCategory(data)
    }

    if (res.code === 1) {
      ElMessage.success(data.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getCategoryList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.type = 1
  formData.sort = 0
}

// 格式化类型
const formatType = (type) => {
  const typeMap = { 1: '菜品分类', 2: '套餐分类' }
  return typeMap[type] || '-'
}

// 格式化状态
const formatStatus = (status) => {
  return status === 1 ? '启用' : '禁用'
}

onMounted(() => {
  getCategoryList()
})
</script>

<template>
  <div class="category-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="分类名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入分类名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增分类</el-button>
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="selectedCategories.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 分类列表 -->
    <el-card class="table-card" shadow="hover" v-loading="loading">
      <el-table
        :data="categoryList"
        @selection-change="selectedCategories = $event"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column label="分类类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'">
              {{ formatType(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
            <span class="status-text" :class="{ active: row.status === 1 }">
              {{ formatStatus(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择分类类型" style="width: 100%">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="999"
            placeholder="请输入排序"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.category-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
}

.search-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.status-text {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}

.status-text.active {
  color: #67c23a;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
