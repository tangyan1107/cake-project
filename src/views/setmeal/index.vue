<script setup>
import { ref, onMounted, reactive } from 'vue'
import { setmealApi } from '@/api/setmeal'
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

// 套餐列表数据
const setmealList = ref([])

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
const dialogTitle = ref('新增套餐')

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: null,
  image: '',
  description: '',
  status: 1,
  setmealDishes: []
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入套餐价格', trigger: 'blur' }],
  image: [{ required: true, message: '请上传套餐图片', trigger: 'change' }]
}

// 选中的套餐（用于批量删除）
const selectedSetmeals = ref([])

// 菜品选择弹窗
const dishDialogVisible = ref(false)
const dishList = ref([])
const selectedDishes = ref([])
const currentCategoryId = ref(null)

// 获取分类列表
const getCategoryList = async () => {
  try {
    const res = await setmealApi.getCategoryList()
    if (res.code === 1) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取套餐列表
const getSetmealList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    const res = await setmealApi.getSetmealPage(params)
    if (res.code === 1) {
      setmealList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取套餐列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getSetmealList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = null
  searchForm.status = null
  pagination.page = 1
  getSetmealList()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  getSetmealList()
}

// 每页数量变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  getSetmealList()
}

// 新增套餐
const handleAdd = () => {
  dialogTitle.value = '新增套餐'
  resetForm()
  dialogVisible.value = true
}

// 编辑套餐
const handleEdit = async (row) => {
  dialogTitle.value = '编辑套餐'
  try {
    const res = await setmealApi.getSetmealById(row.id)
    if (res.code === 1) {
      Object.assign(formData, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取套餐详情失败:', error)
  }
}

// 删除套餐
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该套餐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await setmealApi.deleteSetmeal(row.id.toString())
    if (res.code === 1) {
      ElMessage.success('删除成功')
      getSetmealList()
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
  if (selectedSetmeals.value.length === 0) {
    ElMessage.warning('请选择要删除的套餐')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedSetmeals.value.length} 个套餐吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedSetmeals.value.map(item => item.id).join(',')
    const res = await setmealApi.deleteSetmeal(ids)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      getSetmealList()
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
    const res = await setmealApi.updateSetmealStatus(newStatus, row.id)
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
    
    let res
    if (data.id) {
      // 编辑
      res = await setmealApi.updateSetmeal(data)
    } else {
      // 新增
      res = await setmealApi.addSetmeal(data)
    }

    if (res.code === 1) {
      ElMessage.success(data.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      getSetmealList()
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
  formData.setmealDishes = []
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

// 打开选择菜品弹窗
const openDishDialog = async () => {
  if (!formData.categoryId) {
    ElMessage.warning('请先选择套餐分类')
    return
  }
  currentCategoryId.value = formData.categoryId
  selectedDishes.value = []
  await getDishList()
  dishDialogVisible.value = true
}

// 获取菜品列表
const getDishList = async () => {
  try {
    const res = await setmealApi.getDishListByCategoryId(currentCategoryId.value)
    if (res.code === 1) {
      dishList.value = res.data || []
    }
  } catch (error) {
    console.error('获取菜品列表失败:', error)
  }
}

// 确认选择菜品
const confirmSelectDishes = () => {
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请选择菜品')
    return
  }
  
  // 将选中的菜品添加到套餐菜品列表
  const newDishes = selectedDishes.value.map(dish => ({
    dishId: dish.id,
    name: dish.name,
    price: dish.price,
    copies: 1
  }))
  
  // 去重：如果菜品已存在，不再添加
  newDishes.forEach(dish => {
    const exists = formData.setmealDishes.some(item => item.dishId === dish.dishId)
    if (!exists) {
      formData.setmealDishes.push(dish)
    }
  })
  
  dishDialogVisible.value = false
  ElMessage.success('添加成功')
}

// 删除套餐中的菜品
const removeDish = (index) => {
  formData.setmealDishes.splice(index, 1)
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return price.toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  getCategoryList()
  getSetmealList()
})
</script>

<template>
  <div class="setmeal-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline>
        <el-form-item label="套餐名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入套餐名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="套餐分类">
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
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增套餐</el-button>
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="selectedSetmeals.length === 0"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 套餐列表 -->
    <el-card class="table-card" shadow="hover" v-loading="loading">
      <el-table
        :data="setmealList"
        @selection-change="selectedSetmeals = $event"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="套餐名称" min-width="180">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="套餐图片" width="120" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              fit="cover"
              class="setmeal-thumb-image"
              :preview-src-list="[row.image]"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="套餐分类" width="120" />
        <el-table-column label="套餐价" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'status-enable' : 'status-disable'">
              {{ row.status === 1 ? '启售' : '停售' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              修改
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="warning" 
              link 
              size="small" 
              @click="handleStatusChange(row)"
            >
              停售
            </el-button>
            <el-button 
              v-else 
              type="success" 
              link 
              size="small" 
              @click="handleStatusChange(row)"
            >
              启售
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="套餐分类" prop="categoryId">
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
        <el-form-item label="套餐图片" prop="image">
          <el-upload
            class="setmeal-upload"
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
        <el-form-item label="套餐描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套餐描述"
          />
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启售</el-radio>
            <el-radio :value="0">停售</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="套餐菜品">
          <div class="dish-list-container">
            <el-button type="primary" link @click="openDishDialog">
              <el-icon><Plus /></el-icon>
              添加菜品
            </el-button>
            <el-table :data="formData.setmealDishes" style="width: 100%; margin-top: 10px" border>
              <el-table-column prop="name" label="菜品名称" min-width="120" />
              <el-table-column label="价格" width="100">
                <template #default="{ row }">
                  ¥{{ formatPrice(row.price) }}
                </template>
              </el-table-column>
              <el-table-column label="份数" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.copies" :min="1" :max="99" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ $index }">
                  <el-button type="danger" link size="small" @click="removeDish($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 选择菜品弹窗 -->
    <el-dialog
      v-model="dishDialogVisible"
      title="选择菜品"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-table
        :data="dishList"
        @selection-change="selectedDishes = $event"
        style="width: 100%"
        height="400"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="菜品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            >
              <template #error>
                <div class="image-error-small">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" min-width="150" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            ¥{{ formatPrice(row.price) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="dishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelectDishes">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.setmeal-container {
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

.setmeal-thumb-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.image-error-small {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.status-enable {
  color: #67c23a;
  font-size: 14px;
}

.status-disable {
  color: #909399;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.setmeal-upload {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
}

.setmeal-upload:hover {
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

.dish-list-container {
  width: 100%;
}
</style>
