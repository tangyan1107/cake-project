<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api/employee.js'

const route = useRoute()
const router = useRouter()

// 判断是新增还是编辑
const isEdit = computed(() => !!route.params.id)
const employeeId = computed(() => route.params.id)

// 页面标题
const pageTitle = computed(() => isEdit.value ? '修改员工' : '添加员工')

// 表单数据
const formData = reactive({
  id: undefined,
  username: '',
  name: '',
  phone: '',
  sex: '1',
  idNumber: ''
})

// 表单引用
const formRef = ref(null)

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

// 获取员工详情
const fetchEmployeeDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await employeeApi.getEmployeeById(employeeId.value)
    if (res.code === 1) {
      const data = res.data
      formData.id = data.id
      formData.username = data.username
      formData.name = data.name
      formData.phone = data.phone
      formData.sex = String(data.sex)
      formData.idNumber = data.idNumber
    } else {
      ElMessage.error(res.msg || '获取员工信息失败')
    }
  } catch (error) {
    ElMessage.error('获取员工信息失败')
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
      router.push('/employee')
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
      // 重置表单
      formRef.value.resetFields()
      formData.sex = '1'
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    if (error !== 'validation') {
      ElMessage.error('添加失败')
    }
  }
}

// 返回
const handleBack = () => {
  router.push('/employee')
}

onMounted(() => {
  fetchEmployeeDetail()
})
</script>

<template>
  <div class="employee-form-container">
    <h2 class="page-title">{{ pageTitle }}</h2>
    
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
      class="employee-form"
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
          <el-radio label="1">男</el-radio>
          <el-radio label="2">女</el-radio>
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
    
    <!-- 按钮区域 -->
    <div class="button-area">
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button
        v-if="!isEdit"
        type="primary"
        @click="handleSaveAndContinue"
      >
        保存并继续添加员工
      </el-button>
      <el-button @click="handleBack">返回</el-button>
    </div>
  </div>
</template>

<style scoped>
.employee-form-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
}

.page-title {
  margin-bottom: 30px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.employee-form {
  max-width: 500px;
  margin: 0 auto;
}

.button-area {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
