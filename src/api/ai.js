import axios from 'axios'

// AI助手接口使用8082端口，其他接口使用8080端口
const AI_BASE_URL = 'http://localhost:8082'
const BASE_URL = 'http://localhost:8080'

// 创建axios实例
const request = axios.create({
  baseURL: BASE_URL,
  timeout: 60000 // AI接口可能需要更长的超时时间
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token并添加到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.token = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * AI智能助手API
 */
export const aiApi = {
  /**
   * 发送AI助手请求（流式响应）
   * @param {Object} params - 请求参数
   * @param {string} params.prompt - 用户提问内容
   * @param {string} params.Id - 员工ID（用于会话记忆）
   * @param {Function} onMessage - 处理流式消息的回调函数
   * @param {Function} onComplete - 流式响应完成时的回调函数
   * @param {Function} onError - 错误处理回调函数
   */
  async chatStream(params, onMessage, onComplete, onError) {
    const { prompt, Id } = params
    
    // 构建URL（使用AI助手专用端口8082）
    const url = `${AI_BASE_URL}/admin/ai/help`
    
    // 获取token
    const token = localStorage.getItem('token')
    
    // 构建请求头
    const headers = {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
    if (token) {
      headers['token'] = token
    }
    
    // 构建请求体
    const body = new URLSearchParams()
    body.append('prompt', prompt)
    body.append('Id', Id)
    
    let fullResponse = ''
    let isAborted = false
    
    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: headers,
        body: body
      })
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      
      while (!isAborted) {
        const { done, value } = await reader.read()
        
        if (done) {
          // 处理缓冲区中剩余的数据
          if (buffer.trim()) {
            const lines = buffer.split('\n')
            for (const line of lines) {
              const trimmedLine = line.trim()
              if (!trimmedLine) continue
              
              // 尝试直接解析整行作为JSON
              try {
                const jsonData = JSON.parse(trimmedLine)
                let text = ''
                if (typeof jsonData === 'string') {
                  text = jsonData
                } else if (typeof jsonData === 'object' && jsonData !== null) {
                  text = jsonData.text || ''
                }
                if (text && typeof text === 'string') {
                  text = text.replace(/\\n/g, '\n')
                  fullResponse += text
                  if (onMessage) {
                    onMessage(text, fullResponse)
                  }
                }
                continue
              } catch (e) {
                // 不是JSON，作为纯文本处理
                const text = trimmedLine.replace(/\\n/g, '\n')
                fullResponse += text
                if (onMessage) {
                  onMessage(text, fullResponse)
                }
                continue
              }
              
              if (trimmedLine.startsWith('data:')) {
                const data = trimmedLine.slice(5).trim()
                if (data) {
                  try {
                    // 尝试解析JSON格式
                    const jsonData = JSON.parse(data)
                    let text = jsonData.text || data
                    if (typeof text === 'string') {
                      text = text.replace(/\\n/g, '\n')
                    }
                    fullResponse += text
                    if (onMessage) {
                      onMessage(text, fullResponse)
                    }
                  } catch (e) {
                    // 如果不是JSON，直接使用文本
                    const text = data.replace(/\\n/g, '\n')
                    fullResponse += text
                    if (onMessage) {
                      onMessage(text, fullResponse)
                    }
                  }
                }
              }
            }
          }
          break
        }
        
        // 解码收到的数据并添加到缓冲区
        const chunk = decoder.decode(value, { stream: true })
        console.log('收到原始数据 chunk:', JSON.stringify(chunk))
        buffer += chunk
        
        // 处理缓冲区中的完整行
        const lines = buffer.split('\n')
        // 保留最后一个不完整的行在缓冲区中
        buffer = lines.pop() || ''
        
        for (const line of lines) {
          const trimmedLine = line.trim()
          console.log('处理行:', JSON.stringify(trimmedLine))
          if (!trimmedLine) continue
          
          // 尝试直接解析整行作为JSON（后端直接返回JSON格式）
          try {
            const jsonData = JSON.parse(trimmedLine)
            console.log('解析JSON成功:', jsonData, '类型:', typeof jsonData)
            
            let text = ''
            // 如果解析结果是字符串（后端返回的是纯文本字符串）
            if (typeof jsonData === 'string') {
              text = jsonData
            } else if (typeof jsonData === 'object' && jsonData !== null) {
              // 如果是对象，尝试获取 text 字段
              text = jsonData.text || ''
            }
            
            // 处理转义字符，将 \n 转换为实际换行
            if (text && typeof text === 'string') {
              text = text.replace(/\\n/g, '\n')
              fullResponse += text
              if (onMessage) {
                onMessage(text, fullResponse)
              }
            }
            continue
          } catch (e) {
            // 不是JSON，作为纯文本处理
            console.log('不是JSON格式，作为纯文本处理:', trimmedLine)
            const text = trimmedLine.replace(/\\n/g, '\n')
            fullResponse += text
            if (onMessage) {
              onMessage(text, fullResponse)
            }
            continue
          }
          
          // 检查SSE格式 data: 开头
          if (trimmedLine.startsWith('data:')) {
            const data = trimmedLine.slice(5).trim()
            console.log('提取SSE数据:', JSON.stringify(data))
            if (data) {
              try {
                // 尝试解析JSON格式
                const jsonData = JSON.parse(data)
                console.log('解析SSE内JSON成功:', jsonData)
                let text = jsonData.text || data
                // 处理转义字符
                if (typeof text === 'string') {
                  text = text.replace(/\\n/g, '\n')
                }
                fullResponse += text
                if (onMessage) {
                  onMessage(text, fullResponse)
                }
              } catch (e) {
                console.log('SSE内非JSON数据，直接使用:', data)
                // 如果不是JSON，直接使用文本
                const text = data.replace(/\\n/g, '\n')
                fullResponse += text
                if (onMessage) {
                  onMessage(text, fullResponse)
                }
              }
            }
          }
        }
      }
      
      if (!isAborted && onComplete) {
        onComplete(fullResponse)
      }
    } catch (error) {
      console.error('AI助手请求错误:', error)
      if (!isAborted && onError) {
        onError(error)
      }
    }
    
    // 返回关闭连接的方法
    return {
      close: () => {
        isAborted = true
      }
    }
  },

  /**
   * 获取聊天历史记录
   * @param {string} type - 聊天类型（固定为"chat"）
   * @param {string} Id - 员工ID
   * @returns {Promise<Array>} 返回历史消息列表
   */
  getChatHistory(type, Id) {
    const token = localStorage.getItem('token')
    const headers = {}
    if (token) {
      headers['token'] = token
    }
    
    return fetch(`${AI_BASE_URL}/admin/ai/history/${type}/${Id}`, {
      method: 'GET',
      headers: headers
    }).then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      return response.json()
    })
  }
}

export default aiApi
