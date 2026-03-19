<template>
  <div class="login-container">
    <!-- 登录框 -->
    <div class="login-box">
      <div class="login-left">
        <img src="@/assets/logo.png" alt="Logo" class="login-logo" />
        <h1 class="login-title">甜慕烘培坊管理后台</h1>
      </div>

      <div class="login-right">
        <div class="tab-title">门店管理</div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api/employee'
import websocket from '@/utils/websocket'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于 6 位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await employeeApi.login(loginForm)
        
        if (response.code === 1) {
          // 登录成功，保存 token 到 localStorage
          localStorage.setItem('token', response.data.token)
          localStorage.setItem('userInfo', JSON.stringify(response.data))
          
          ElMessage.success('登录成功')
          
          // 建立 WebSocket 连接
          try {
            const userId = response.data.id || response.data.sid || 'default'
            await websocket.initWebSocket(userId)
            console.log('✅ WebSocket 连接已建立')
          } catch (wsError) {
            console.warn('WebSocket 连接失败，但不影响使用:', wsError)
          }
          
          // 跳转到工作台
          router.push('/dashboard')
        } else {
          ElMessage.error(response.msg || '登录失败')
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '登录失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 100%;
  max-width: 900px;
  min-height: 450px;
  background-color: #fff;
  border: 1px solid #e8eaec;
  display: flex;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background-color: #f3ecc8;
}

.login-logo {
  width: auto;
  height: 80px;
  margin-bottom: 20px;
  object-fit: contain;
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  text-align: center;
}

.login-right {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.tab-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8eaec;
}

.login-form {
  width: 100%;
}

.login-form .el-form-item {
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  margin-top: 20px;
}
</style>
