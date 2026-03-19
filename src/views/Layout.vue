<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import {
  HomeFilled,
  Document,
  Menu,
  Food,
  Dish,
  DataLine,
  UserFilled,
  Bell,
  ArrowDown
} from '@element-plus/icons-vue'
import websocket from '@/utils/websocket'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)

// 营业状态：true-营业中，false-休息中
const isBusiness = ref(true)

// WebSocket 消息计数
const messageCount = ref(0)

// 菜单配置
const menuList = [
  { path: '/dashboard', title: '工作台', icon: HomeFilled },
  { path: '/order', title: '订单管理', icon: Document },
  { path: '/category', title: '分类管理', icon: Menu },
  { path: '/dish', title: '菜品管理', icon: Food },
  { path: '/setmeal', title: '套餐管理', icon: Dish },
  { path: '/statistics', title: '数据统计', icon: DataLine },
  { path: '/employee', title: '员工管理', icon: UserFilled }
]

const handleMenuSelect = (path) => {
  router.push(path)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    // 退出登录逻辑
    // 关闭 WebSocket 连接
    websocket.closeWebSocket()
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

// 切换营业状态
const toggleBusinessStatus = () => {
  ElMessageBox.confirm(
    `确定要切换到${isBusiness.value ? '休息' : '营业'}状态吗？`,
    '营业状态切换',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: isBusiness.value ? 'warning' : 'success'
    }
  ).then(() => {
    isBusiness.value = !isBusiness.value
    ElMessage.success(`已切换到${isBusiness.value ? '营业' : '休息'}状态`)
  }).catch(() => {
    // 用户取消
  })
}

// 监听 WebSocket 消息
let removeMessageListener = null
let removeCloseListener = null

// 播放提示铃声
const playNotificationSound = async () => {
  return new Promise((resolve) => {
    try {
      const AudioContextClass = window.AudioContext || window.webkitAudioContext
      if (AudioContextClass) {
        const audioCtx = new AudioContextClass()
        
        // 恢复音频上下文（处理浏览器自动播放限制）
        if (audioCtx.state === 'suspended') {
          audioCtx.resume().catch(() => {})
        }
        
        // 创建两个振荡器，模拟"叮"和"咚"
        const oscillator1 = audioCtx.createOscillator()
        const oscillator2 = audioCtx.createOscillator()
        const gainNode = audioCtx.createGain()
        
        oscillator1.connect(gainNode)
        oscillator2.connect(gainNode)
        gainNode.connect(audioCtx.destination)
        
        // "叮" - 高频音 (880Hz)
        oscillator1.type = 'sine'
        oscillator1.frequency.setValueAtTime(880, audioCtx.currentTime)
        
        // "咚" - 低频音 (660Hz)，延迟 0.15 秒
        oscillator2.type = 'sine'
        oscillator2.frequency.setValueAtTime(660, audioCtx.currentTime + 0.15)
        
        // 音量控制
        gainNode.gain.setValueAtTime(0.4, audioCtx.currentTime)
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.15)
        gainNode.gain.setValueAtTime(0.4, audioCtx.currentTime + 0.15)
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.3)
        
        // 启动振荡器
        oscillator1.start(audioCtx.currentTime)
        oscillator1.stop(audioCtx.currentTime + 0.15)
        
        oscillator2.start(audioCtx.currentTime + 0.15)
        oscillator2.stop(audioCtx.currentTime + 0.3)
        
        setTimeout(() => { 
          resolve() 
        }, 350)
      } else {
        console.warn('浏览器不支持 AudioContext')
        resolve()
      }
    } catch (error) {
      console.warn('音频播放失败:', error)
      resolve() // 即使失败也继续执行
    }
  })
}

// 语音播报函数
const speakMessage = async (text) => {
  console.log('准备播报:', text)
  
  // 先播放提示铃声
  try {
    await playNotificationSound()
  } catch (error) {
    console.warn('铃声播放失败:', error)
  }
  
  // 使用 speechSynthesis 播报
  if ('speechSynthesis' in window) {
    // 取消之前的播报
    window.speechSynthesis.cancel()
    
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.9
    utterance.pitch = 1.0
    utterance.volume = 1.0
    
    utterance.onstart = () => console.log('🔊 开始播报')
    utterance.onend = () => console.log('✅ 播报结束')
    utterance.onerror = (event) => {
      console.warn(' 播报错误:', event.error)
      // 如果是 'not-allowed' 错误，尝试用户交互后重新播报
      if (event.error === 'not-allowed' || event.error === 'interrupted') {
        console.log('等待用户交互后重试...')
        setTimeout(() => {
          try {
            window.speechSynthesis.speak(utterance)
          } catch (e) {
            console.warn('重试失败:', e)
          }
        }, 100)
      }
    }
    
    try {
      window.speechSynthesis.speak(utterance)
      console.log('🎵 已添加到播报队列')
    } catch (error) {
      console.warn('语音播报失败:', error)
    }
  } else {
    console.warn('浏览器不支持语音合成')
  }
}

// 显示订单通知弹窗
const showOrderNotification = (orderNumber, type) => {
  console.log('🔔 准备显示通知:', { orderNumber, type })
  
  const isNewOrder = type === 1
  
  try {
    // 使用 Element Plus 的通知组件
    ElNotification({
      title: isNewOrder ? '新订单通知' : '客户催单',
      message: `订单号：${orderNumber}`,
      position: 'top-right',
      duration: 3000,
      type: isNewOrder ? 'success' : 'warning',
      customClass: 'order-notification'
    })
    console.log('✅ ElNotification 调用成功')
  } catch (error) {
    console.error('❌ ElNotification 调用失败:', error)
    // 如果 ElNotification 失败，降级使用 ElMessage
    ElMessage({
      message: `${isNewOrder ? '新订单' : '客户催单'}：${orderNumber}`,
      type: isNewOrder ? 'success' : 'warning',
      duration: 3000
    })
    console.log('✅ 已降级使用 ElMessage')
  }
}

// 组件挂载时建立 WebSocket 连接
onMounted(() => {
  console.log('🔍 页面加载，检查 WebSocket...')
  
  // 如果还没有建立连接，尝试建立连接
  if (!websocket.isConnected()) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    console.log(' 用户信息:', userInfo)
    
    const userId = userInfo.id || userInfo.sid || 'default'
    console.log(' 使用用户 ID:', userId)
    
    websocket.initWebSocket(userId)
      .then(() => {
        console.log('✅ WebSocket 连接已建立')
        
        // 监听消息
        registerMessageListener()
        
        // 监听连接关闭
        removeCloseListener = websocket.onClose(() => {
          console.log('❌ WebSocket 连接已关闭')
          messageCount.value = 0
        })
      })
      .catch((error) => {
        console.error('❌ WebSocket 连接失败:', error)
      })
  } else {
    console.log('⚠️ WebSocket 已经连接，直接注册监听器')
    // WebSocket 已连接，直接注册监听器
    registerMessageListener()
  }
})

// 注册消息监听器
const registerMessageListener = () => {
  console.log('🔔 Layout: 注册 WebSocket 消息监听器')
  removeMessageListener.value = websocket.onMessage((message) => {
    console.log('📩 Layout: 收到 WebSocket 消息:', message)
    console.log('📊 消息类型:', typeof message)
    messageCount.value++
    
    // 处理不同类型的消息
    try {
      const msgData = typeof message === 'string' ? JSON.parse(message) : message
      console.log('🔍 Layout: 解析后的消息对象:', msgData)
      
      // 根据 type 字段处理不同类型的消息
      // type: 1 - 来单提醒，type: 2 - 客户催单
      if (msgData.type === 1) {
        console.log('🎯 Layout: 检测到 type=1，处理来单提醒')
        
        // 显示自定义通知
        showOrderNotification(msgData.content.replace('订单号:', ''), 1)
        
        // 语音播报
        speakMessage('您有新的订单')
        
      } else if (msgData.type === 2) {
        console.log('⚠️ Layout: 检测到 type=2，处理客户催单')
        
        // 显示自定义通知
        showOrderNotification(msgData.content, 2)
        
        // 语音播报
        speakMessage('有用户催单了')
        
      } else {
        console.log('ℹ️ 其他类型消息:', msgData.type)
        const normalText = msgData.content || message
        
        ElMessage({
          message: normalText,
          type: 'info',
          duration: 4000,
          showClose: true
        })
        speakMessage(normalText)
      }
    } catch (e) {
      console.error('❌ 消息解析错误:', e)
      ElMessage({
        message: message,
        type: 'info',
        duration: 4000,
        showClose: true
      })
      speakMessage(message)
    }
  })
}

// 组件卸载时清理
onUnmounted(() => {
  // 移除监听器
  if (removeMessageListener) {
    removeMessageListener()
  }
  if (removeCloseListener) {
    removeCloseListener()
  }
  // 注意：不在这里关闭 WebSocket，因为可能在其他页面还需要使用
})
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside class="sidebar" :width="isCollapse ? '64px' : '200px'">
      <div class="logo">
        <img src="@/assets/logo.png" alt="logo" class="logo-img" />
      </div>
      <el-menu
        :default-active="route.path"
        class="sidebar-menu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#f2b6d4"
        text-color="#333333"
        active-text-color="#666666"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuList" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-tag 
            :type="isBusiness ? 'danger' : 'info'" 
            :effect="isBusiness ? 'dark' : 'plain'"
            size="large"
          >
            {{ isBusiness ? '营业中' : '休息中' }}
          </el-tag>
        </div>
        <div class="header-right">
          <el-button type="primary" size="small" @click="toggleBusinessStatus">营业状态</el-button>
          <el-badge :value="messageCount" :is-dot="messageCount > 0" class="message-badge">
            <el-icon :size="24"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.sidebar {
  background-color: #f3ecc8;
  transition: width 0.3s;
  height: 100vh;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3ecc8;
  white-space: nowrap;
  overflow: hidden;
}

.logo-img {
  height: 40px;
  width: auto;
  object-fit: contain;
}

.sidebar-menu {
  border-right: none;
  background-color: #f3ecc8;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #333333;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color:#f3ecc8;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: #409EFF;
  background-color: #fff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

/* 订单通知弹窗样式 - Element Plus Notification 定制 */
.el-notification.order-notification {
  border-left: 4px solid #ff6b81 !important;
  background: linear-gradient(135deg, #fff5f6 0%, #fff 100%) !important;
}

.el-notification.order-notification .el-notification__title {
  color: #ff6b81 !important;
}

.el-notification.order-notification .el-notification__content {
  font-size: 14px;
  color: #606266;
}

/* 铃铛图标动画 */
.el-notification .bell-icon {
  animation: ring 0.5s ease-in-out;
}

@keyframes ring {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-15deg); }
  50% { transform: rotate(15deg); }
  75% { transform: rotate(-15deg); }
}
</style>
