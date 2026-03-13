<script setup>
import { ref, onMounted, reactive } from 'vue'
import { setmealApi } from '@/api/setmeal'
import { categoryApi } from '@/api/category'
import { dishApi } from '@/api/dish'
import { commonApi } from '@/api/common'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh, Picture, Close } from '@element-plus/icons-vue'

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
  status: null,
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

// 菜品分类列表
const dishCategoryList = ref([])
const selectedDishCategory = ref(null)
const searchKeyword = ref('')

// 已选菜品列表（右侧显示）
const addedDishes = ref([])

// 获取分类列表 - 查询套餐分类 (type=2)
const getCategoryList = async () => {
  try {
    const res = await categoryApi.getCategoryByType(2)
    if (res.code === 1) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取套餐分类列表失败:', error)
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
      // 根据接口文档，数据在 res.data 中
      const data = res.data
      // 重置表单后赋值，确保数据正确回显
      resetForm()
      // 回显基本信息
      formData.id = data.id
      formData.name = data.name
      formData.categoryId = data.categoryId
      formData.price = data.price
      formData.image = data.image
      formData.description = data.description
      formData.status = data.status
      // 回显套餐菜品列表 - 根据返回数据的实际结构调整
      // 后端可能返回 setmealDishes 或 setMealDishes 字段
      if (data.setmealDishes && Array.isArray(data.setmealDishes)) {
        formData.setmealDishes = data.setmealDishes.map(dish => ({
          dishId: dish.dishId,
          name: dish.name,
          price: dish.price,
          copies: dish.copies || 1
        }))
      } else if (data.setMealDishes && Array.isArray(data.setMealDishes)) {
        formData.setmealDishes = data.setMealDishes.map(dish => ({
          dishId: dish.dishId,
          name: dish.name,
          price: dish.price,
          copies: dish.copies || 1
        }))
      }
      dialogVisible.value = true
    } else {
      ElMessage.error(res.msg || '获取套餐详情失败')
    }
  } catch (error) {
    console.error('获取套餐详情失败:', error)
    ElMessage.error('获取套餐详情失败，请稍后重试')
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
      // 显示后端返回的错误信息
      const errorMsg = error.response?.data?.msg || error.msg || '删除失败，请检查是否满足删除条件'
      ElMessage.error(errorMsg)
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
      // 显示后端返回的错误信息
      const errorMsg = error.response?.data?.msg || error.msg || '删除失败，请检查是否满足删除条件'
      ElMessage.error(errorMsg)
    }
  }
}

// 切换状态
const handleStatusChange = async (row) => {
  const targetStatus = row.status
  const statusText = targetStatus === 1 ? '启售' : '停售'
  try {
    const res = await setmealApi.updateSetmealStatus(targetStatus, row.id)
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
  formData.status = null
  formData.setmealDishes = []
}

// 图片上传处理
const handleImageChange = async (file) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  try {
    // 调用上传接口
    const res = await commonApi.upload(file.raw)
    if (res.code === 1) {
      formData.image = res.data // 保存返回的文件路径
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  }
  return false
}

// 打开选择菜品弹窗
const openDishDialog = async () => {
  if (!formData.categoryId) {
    ElMessage.warning('请先选择套餐分类')
    return
  }
  // 获取菜品分类列表
  await getDishCategoryList()
  // 默认选中第一个分类
  if (dishCategoryList.value.length > 0) {
    selectedDishCategory.value = dishCategoryList.value[0].id
  }
  addedDishes.value = [...formData.setmealDishes]
  searchKeyword.value = ''
  dishDialogVisible.value = true
  await getDishList()
}

// 获取菜品分类列表
const getDishCategoryList = async () => {
  try {
    const res = await categoryApi.getCategoryByType(1) // type=1 为菜品分类
    if (res.code === 1) {
      dishCategoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取菜品分类列表失败:', error)
  }
}

// 获取菜品列表 - 根据分类 id 查询
const getDishList = async () => {
  if (!selectedDishCategory.value) {
    dishList.value = []
    return
  }
  try {
    const res = await dishApi.getDishListByCategoryId(selectedDishCategory.value)
    if (res.code === 1) {
      let dishes = res.data || []
      // 如果有搜索关键词，进行过滤
      if (searchKeyword.value) {
        dishes = dishes.filter(dish => 
          dish.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
        )
      }
      dishList.value = dishes
    }
  } catch (error) {
    console.error('获取菜品列表失败:', error)
  }
}

// 切换分类时刷新菜品列表
const handleCategoryChange = () => {
  getDishList()
}

// 搜索菜品
const handleSearchDish = () => {
  getDishList()
}

// 确认添加菜品
const confirmAddDishes = () => {
  formData.setmealDishes = [...addedDishes.value]
  dishDialogVisible.value = false
  ElMessage.success('添加成功')
}

// 从已选列表中移除
const removeAddedDish = (index) => {
  addedDishes.value.splice(index, 1)
}

// 切换菜品选择状态
const toggleDishSelection = (dish) => {
  const index = addedDishes.value.findIndex(d => d.dishId === dish.id)
  if (index > -1) {
    // 已存在，移除
    addedDishes.value.splice(index, 1)
  } else {
    // 不存在，添加
    addedDishes.value.push({
      dishId: dish.id,
      name: dish.name,
      price: dish.price,
      copies: 1
    })
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

// 获取完整图片路径
const getImageUrl = (path) => {
  if (!path) return ''
  // 如果已经是完整路径，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  // 否则拼接基础路径
  return `http://localhost:8080${path}`
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
        <el-table-column label="售卖状态" width="120">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
            <span class="status-text" :class="{ active: row.status === 1 }">
              {{ row.status === 1 ? '启售' : '停售' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              修改
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
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
          <div style="display: flex; align-items: flex-start; gap: 20px">
            <el-upload
              class="setmeal-upload"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
            >
              <el-image
                v-if="formData.image"
                :src="getImageUrl(formData.image)"
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
            <div class="upload-tips">
              <p>图片大小不超过 2M</p>
              <p>仅能上传 PNG JPEG JPG 类型图片</p>
              <p>建议上传 200*200 或 300*300 尺寸的图片</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="套餐描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套餐描述"
          />
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
      title="添加菜品"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="dish-selection-container">
        <!-- 左侧：分类和菜品列表 -->
        <div class="dish-list-section">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="请输入菜品名称进行搜索"
              prefix-icon="Search"
              clearable
              @keyup.enter="handleSearchDish"
            />
          </div>
          
          <div class="dish-content">
            <!-- 左侧分类 -->
            <div class="category-column">
              <div 
                v-for="category in dishCategoryList" 
                :key="category.id"
                :class="['category-item', { active: selectedDishCategory === category.id }]"
                @click="selectedDishCategory = category.id; handleCategoryChange()"
              >
                {{ category.name }}
              </div>
            </div>
            
            <!-- 中间菜品列表 -->
            <div class="dishes-column">
              <div 
                v-for="dish in dishList" 
                :key="dish.id"
                :class="['dish-item', { selected: addedDishes.some(d => d.dishId === dish.id) }]"
                @click="toggleDishSelection(dish)"
              >
                <el-checkbox 
                  :model-value="addedDishes.some(d => d.dishId === dish.id)"
                  @click.stop="toggleDishSelection(dish)"
                />
                <span class="dish-name">{{ dish.name }}</span>
                <span class="dish-status">{{ dish.status === 1 ? '在售' : '停售' }}</span>
                <span class="dish-price">{{ dish.price }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧：已选菜品 -->
        <div class="selected-section">
          <div class="selected-header">
            已选菜品 ({{ addedDishes.length }})
          </div>
          <div class="selected-dishes-list">
            <div 
              v-for="(dish, index) in addedDishes" 
              :key="dish.dishId"
              class="selected-dish-item"
            >
              <div class="selected-dish-info">
                <span class="dish-name">{{ dish.name }}</span>
                <span class="dish-price">¥ {{ dish.price }}</span>
              </div>
              <el-button 
                type="danger" 
                link 
                size="small"
                @click="removeAddedDish(index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="dishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddDishes">添加</el-button>
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

.upload-tips {
  flex: 1;
  min-width: 200px;
}

.upload-tips p {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.dish-list-container {
  width: 100%;
}

/* 选择菜品弹窗样式 */
.dish-selection-container {
  display: flex;
  gap: 20px;
  height: 500px;
}

.dish-list-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.search-box {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.dish-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.category-column {
  width: 150px;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  background-color: #fafafa;
}

.category-item {
  padding: 12px 15px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.category-item:hover {
  background-color: #f5f5f5;
}

.category-item.active {
  background-color: #fff;
  color: #409eff;
  font-weight: 600;
  border-left: 3px solid #409eff;
}

.dishes-column {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.dish-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  margin-bottom: 8px;
  border-radius: 4px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.dish-item:hover {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

.dish-item.selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.dish-name {
  flex: 1;
  margin-left: 10px;
  font-size: 14px;
}

.dish-status {
  color: #67c23a;
  font-size: 12px;
  margin-right: 15px;
}

.dish-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

.selected-section {
  width: 300px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.selected-header {
  padding: 15px;
  background-color: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  font-size: 14px;
}

.selected-dishes-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.selected-dish-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.selected-dish-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-dish-info .dish-name {
  margin-left: 0;
}

.selected-dish-info .dish-price {
  font-size: 13px;
}
</style>
