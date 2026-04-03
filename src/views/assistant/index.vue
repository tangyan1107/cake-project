<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'
import { ChatDotRound, User, Promotion } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'

// 消息列表
const messages = ref([
  {
    type: 'assistant',
    content: '你好，我是甜慕烘培AI助手小慕，有什么可以帮到您？',
    time: new Date()
  }
])

// 初始化 Markdown 解析器
const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true
})

// 输入框内容
const inputMessage = ref('')

// 是否正在加载中
const loading = ref(false)

// 当前流式连接
const currentStream = ref(null)

// 消息容器引用
const messageContainerRef = ref(null)

// 获取当前登录员工ID
const getEmployeeId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.id || userInfo.sid || '1'
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messageContainerRef.value) {
    messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (date) => {
  const now = new Date(date)
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 发送消息
const sendMessage = async () => {
  const prompt = inputMessage.value.trim()
  if (!prompt) {
    ElMessage.warning('请输入您的问题')
    return
  }

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
    time: new Date()
  })

  // 清空输入框
  inputMessage.value = ''

  // 滚动到底部
  scrollToBottom()

  // 设置加载状态
  loading.value = true

  // 添加助手消息占位（用于流式显示）
  const assistantMessageIndex = messages.value.length
  messages.value.push({
    type: 'assistant',
    content: '',
    time: new Date(),
    isStreaming: true
  })

  // 获取员工ID
  const employeeId = getEmployeeId()

  // 调用流式API
  currentStream.value = aiApi.chatStream(
    {
      prompt: prompt,
      Id: employeeId
    },
    // onMessage - 收到流式消息时更新
    (chunk, fullText) => {
      messages.value[assistantMessageIndex].content = fullText
      scrollToBottom()
    },
    // onComplete - 流式响应完成
    (fullText) => {
      loading.value = false
      messages.value[assistantMessageIndex].isStreaming = false
      currentStream.value = null
      scrollToBottom()
    },
    // onError - 错误处理
    (error) => {
      loading.value = false
      messages.value[assistantMessageIndex].isStreaming = false
      messages.value[assistantMessageIndex].content = '抱歉，服务暂时不可用，请稍后再试。'
      currentStream.value = null
      ElMessage.error('请求失败，请稍后重试')
      scrollToBottom()
    }
  )
}

// 处理回车发送
const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 加载历史聊天记录
const loadChatHistory = async () => {
  try {
    const employeeId = getEmployeeId()
    const history = await aiApi.getChatHistory('chat', employeeId)
    
    if (history && history.length > 0) {
      // 转换历史记录格式
      const historyMessages = history.map(item => ({
        type: item.role === 'user' ? 'user' : 'assistant',
        content: item.content,
        time: new Date()
      }))
      
      // 替换初始欢迎语，使用历史记录
      messages.value = historyMessages
      
      // 历史记录加载完成后滚动到底部
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    // 加载失败时保持默认欢迎语
  }
}

// 页面加载时
onMounted(() => {
  loadChatHistory()
})
</script>

<template>
  <div class="assistant-container">
    <!-- 页面标题 -->
    <div class="assistant-header">
      <h2>商家助手</h2>
      <p>AI智能助手，随时为您解答问题</p>
    </div>

    <!-- 聊天区域 -->
    <el-card class="chat-card" shadow="hover">
      <!-- 消息列表 -->
      <div ref="messageContainerRef" class="message-container">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message-item"
          :class="message.type"
        >
          <!-- 头像 -->
          <div class="avatar">
            <el-avatar
              v-if="message.type === 'user'"
              :size="40"
              :icon="User"
              class="user-avatar"
            />
            <el-avatar
              v-else
              :size="40"
              :icon="ChatDotRound"
              class="assistant-avatar"
            />
          </div>

          <!-- 消息内容 -->
          <div class="message-content-wrapper">
            <div class="message-header">
              <span class="message-sender">
                {{ message.type === 'user' ? '我' : '小慕' }}
              </span>
              <span class="message-time">{{ formatTime(message.time) }}</span>
            </div>
            <div class="message-bubble" :class="message.type">
              <div 
                v-if="message.type === 'assistant'" 
                class="message-text markdown-body" 
                v-html="md.render(message.content)"
              ></div>
              <div v-else class="message-text">{{ message.content }}</div>
              <div v-if="message.isStreaming" class="typing-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 常见问题 -->
      <div class="quick-questions-bar">
        <div
          v-for="(question, index) in [
            '今天适合上新什么菜品',
            '今日最高销量',
            '是否有菜品需要停售/起售',
            '帮我分析近一周的营业额'
          ]"
          :key="index"
          class="question-item"
          @click="inputMessage = question"
        >
          {{ question }}
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="请输入您的问题，按Enter发送..."
          :disabled="loading"
          @keydown="handleKeydown"
          resize="none"
          class="chat-input"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          :loading="loading"
          @click="sendMessage"
          class="send-btn"
        >
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.assistant-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
}

.assistant-header {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.assistant-header h2 {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.assistant-header p {
  color: #909399;
  font-size: 13px;
  margin: 0;
}

.chat-card {
  margin-bottom: 0;
}

.chat-card :deep(.el-card__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 220px);
}

.message-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: rgba(243, 236, 200, 0.4);
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.message-item.user {
  flex-direction: row-reverse;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.avatar {
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #a8d8ea 0%, #b8e6f0 100%);
}

.assistant-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-item.user .message-content-wrapper {
  align-items: flex-end;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-item.user .message-header {
  flex-direction: row-reverse;
}

.message-sender {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  word-break: break-all;
}

.message-bubble.assistant {
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 4px;
  color: #303133;
}

.message-bubble.user {
  background: linear-gradient(135deg, #f2b6d4 0%, #f5c6e0 100%);
  color: #333;
  border-top-right-radius: 4px;
}

.message-text {
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 0;
}

.typing-indicator .dot {
  width: 6px;
  height: 6px;
  background-color: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
}

.send-btn {
  align-self: flex-end;
  height: 40px;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 500;
}

/* 常见问题条 */
.quick-questions-bar {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  background-color: rgba(243, 236, 200, 0.4);
  border-top: 1px solid transparent;
  overflow-x: auto;
  white-space: nowrap;
}

.quick-questions-bar::-webkit-scrollbar {
  height: 4px;
}

.quick-questions-bar::-webkit-scrollbar-track {
  background: transparent;
}

.quick-questions-bar::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 2px;
}

.question-item {
  flex-shrink: 0;
  padding: 6px 16px;
  font-size: 13px;
  color: #909399;
  background-color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(220, 223, 230, 0.6);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-item:hover {
  color: #409eff;
  border-color: #409eff;
  background-color: rgba(236, 245, 255, 0.9);
}

/* 滚动条样式 */
.message-container::-webkit-scrollbar {
  width: 6px;
}

.message-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-container::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.message-container::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* Markdown 样式 */
.markdown-body {
  line-height: 1.6;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 12px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body :deep(h1) { font-size: 1.5em; }
.markdown-body :deep(h2) { font-size: 1.3em; }
.markdown-body :deep(h3) { font-size: 1.15em; }

.markdown-body :deep(p) {
  margin: 8px 0;
}

.markdown-body :deep(strong) {
  font-weight: 600;
  color: #303133;
}

.markdown-body :deep(em) {
  font-style: italic;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(code) {
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
  color: #d63384;
}

.markdown-body :deep(pre) {
  background-color: #f4f4f5;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-body :deep(pre code) {
  background-color: transparent;
  padding: 0;
  color: #303133;
}

.markdown-body :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #dcdfe6;
  padding-left: 12px;
  margin: 8px 0;
  color: #606266;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style>
