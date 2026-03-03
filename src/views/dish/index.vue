<script setup>
import { ref, onMounted, reactive } from 'vue'
import { dishApi } from '@/api/dish'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Picture } from '@element-plus/icons-vue'

// 搜索表单数据
const searchForm = reactive({
  name: '',
  categoryId: null,
  status: null
})

// 分类列表
const categoryList = ref([])

// 菜品列表数据
const dishList = ref([])

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
const dialogTitle = ref('新增菜品')

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: null,
  image: '',
  description: '',
  status: 1,
  flavors: []
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择菜品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入菜品价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传菜品图片', trigger: 'change' }]
}

// 选中的菜品（用于批量删除）
const selectedDishes = ref([])

// 获取分类列表
const getCategoryList = async () => {
  try {
    const res = await dishApi.getCategoryList()
    if (res.code === 1) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取菜品列表
const getDishList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    const res = await dishApi.getDishPage(params)
    if (res.code === 1) {
      dishList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取菜品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getDishList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = null
  searchForm.status = null
  pagination.page = 1
  getDishList()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  getDishList()
}

// 每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  getDishList()
}

// 新增菜品
const handleAdd = () => {
  dialogTitle.value = '新增菜品'
  resetForm()
  dialogVisible.value = true
}

// 编辑菜品
const handleEdit = async (row) => {
  dialogTitle.value = '编辑菜品'
  try {
    const res = await dishApi.getDishById(row.id)
    if (res.code === 1) {
      Object.assign(formData, res.data)
      // 处理口味数据
      if (formData.flavors) {
        formData.flavors = formData.flavors.map(f => ({
          name: f.name,
          value: f.value
        }))
      } else {
        formData.flavors = []
      }
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取菜品详情失败:', error)
  }
}

// 删除菜品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await dishApi.deleteDish(row.id.toString())
    if (res.code === 1) {
      ElMessage.success('删除成功')
      getDishList()
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
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请选择要删除的菜品')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedDishes.value.length} 个菜品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedDishes.value.map(item => item.id).join(',')
    const res = await dishApi.deleteDish(ids)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      getDishList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 切换状态
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启售' : '停售'
  try {
    const res = await dishApi.updateDishStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success(`${statusText}成功`)
      row.status = newStatus
    } else {
      ElMessage.error(res.msg || `${statusText}失败`)
      // 恢复原状态
      row.status = newStatus === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('修改状态失败:', error)
    // 恢复原状态
    row.status = newStatus === 1 ? 0 : 1
  }
}

// 提交表单
const submitForm = async () => {
  try {
    const data = { ...formData }
    // 处理口味数据
    if (data.flavors && data.flavors.length > 0) {
      data.flavors = data.flavors.map(f => ({
        name: f.name,
        value: JSON.stringify(f.value)
      }))
    }

    let res
    if (data.id) {
      // 编辑
      res = await dishApi.updateDish(data)
    } else {
      // 新增
      res = await dishApi.addDish(data)
    }

    if (res.code === 1) {
      ElMessage.success(data.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getDishList()
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
  formData.categoryId = null
  formData.price = null
  formData.image = ''
  formData.description = ''
  formData.status = 1
  formData.flavors = []
}

// 图片上传处理
const handleImageChange = (file) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt5M = file.raw.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }

  // 这里应该调用上传接口，这里用本地预览演示
  const url = URL.createObjectURL(file.raw)
  formData.image = url
  return false
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return price.toFixed(2)
}

// 格式化状态
const formatStatus = (status) => {
  return status === 1 ? '启售' : '停售'
}

onMounted(() => {
  getCategoryList()
  getDishList()
})
</script>

<template>
  <div class="dish-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="菜品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入菜品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启售" :value="1" />
            <el-option label="停售" :value="0" />
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
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增菜品</el-button>
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="selectedDishes.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 菜品列表 -->
    <el-card class="table-card" shadow="hover" v-loading="loading">
      <el-table
        :data="dishList"
        @selection-change="selectedDishes = $event"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="菜品" min-width="200">
          <template #default="{ row }">
            <div class="dish-info">
              <el-image
                :src="row.image"
                fit="cover"
                class="dish-image"
                :preview-src-list="[row.image]"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <span class="dish-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="菜品分类" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态" width="100">
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
        <el-table-column prop="description" label="菜品描述" min-width="150" show-overflow-tooltip />
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0"
            :precision="2"
            placeholder="请输入价格"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜品图片" prop="image">
          <el-upload
            class="dish-upload"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
          >
            <el-image
              v-if="formData.image"
              :src="formData.image"
              fit="cover"
              class="upload-image"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="upload-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="菜品描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入菜品描述"
          />
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启售</el-radio>
            <el-radio :value="0">停售</el-radio>
          </el-radio-group>
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
.dish-container {
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

.dish-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dish-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  flex-shrink: 0;
}

.dish-name {
  font-size: 14px;
  color: #303133;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.price {
  color: #f56c6c;
  font-weight: 600;
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

.dish-upload {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
}

.dish-upload:hover {
  border-color: #409eff;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c939d;
}

.upload-placeholder span {
  font-size: 12px;
  margin-top: 8px;
}
</style>
