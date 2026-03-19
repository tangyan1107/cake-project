/**
 * WebSocket 工具类
 */

let webSocket = null;
let sid = null;

// WebSocket 服务器地址（根据实际后端地址配置）
const WS_SERVER_URL = 'ws://localhost:8080/ws';

/**
 * 初始化 WebSocket 连接
 * @param {string} sessionId - 会话 ID
 * @returns {Promise}
 */
export function initWebSocket(sessionId) {
  return new Promise((resolve, reject) => {
    if (!sessionId) {
      reject(new Error('会话 ID 不能为空'));
      return;
    }

    sid = sessionId;
    const url = `${WS_SERVER_URL}/${sid}`;

    // 检查浏览器是否支持 WebSocket
    if (!('WebSocket' in window)) {
      reject(new Error('您的浏览器不支持 WebSocket'));
      return;
    }

    try {
      webSocket = new WebSocket(url);

      // 连接成功
      webSocket.onopen = (event) => {
        console.log('WebSocket 连接建立成功', event);
        resolve({ type: 'open', event });
      };

      // 收到消息
      webSocket.onmessage = (event) => {
        console.log('📨 收到服务器消息:', event.data);
        
        // 触发全局消息事件
        window.dispatchEvent(new CustomEvent('ws-message', { 
          detail: event.data,
          timestamp: new Date().getTime()
        }));
      };

      // 连接错误
      webSocket.onerror = (event) => {
        console.error('WebSocket 连接错误', event);
        reject(event);
      };

      // 连接关闭
      webSocket.onclose = (event) => {
        console.log('WebSocket 连接关闭', event);
        window.dispatchEvent(new CustomEvent('ws-close', { detail: event }));
      };
    } catch (error) {
      console.error('创建 WebSocket 连接失败', error);
      reject(error);
    }
  });
}

/**
 * 发送消息到服务器
 * @param {string|object} message - 要发送的消息
 */
export function sendToServer(message) {
  if (!webSocket || webSocket.readyState !== WebSocket.OPEN) {
    console.error('WebSocket 未连接或连接已关闭');
    return false;
  }

  // 如果消息是对象，转换为 JSON 字符串
  const messageStr = typeof message === 'object' 
    ? JSON.stringify(message) 
    : message;

  webSocket.send(messageStr);
  console.log('发送消息到服务器:', messageStr);
  return true;
}

/**
 * 关闭 WebSocket 连接
 */
export function closeWebSocket() {
  if (webSocket) {
    webSocket.close();
    webSocket = null;
    sid = null;
    console.log('WebSocket 连接已关闭');
  }
}

/**
 * 获取 WebSocket 连接状态
 * @returns {number|null}
 * 0: CONNECTING - 正在连接
 * 1: OPEN - 已连接
 * 2: CLOSING - 正在关闭
 * 3: CLOSED - 已关闭
 */
export function getWebSocketState() {
  if (!webSocket) {
    return null;
  }
  return webSocket.readyState;
}

/**
 * 检查 WebSocket 是否已连接
 * @returns {boolean}
 */
export function isConnected() {
  return webSocket && webSocket.readyState === WebSocket.OPEN;
}

/**
 * 监听 WebSocket 消息
 * @param {function} callback - 消息回调函数
 * @returns {function} - 取消监听的函数
 */
export function onMessage(callback) {
  const handler = (event) => {
    callback(event.detail);
  };
  
  window.addEventListener('ws-message', handler);
  
  // 返回取消监听的函数
  return () => {
    window.removeEventListener('ws-message', handler);
  };
}

/**
 * 监听 WebSocket 关闭事件
 * @param {function} callback - 关闭回调函数
 * @returns {function} - 取消监听的函数
 */
export function onClose(callback) {
  const handler = (event) => {
    callback(event.detail);
  };
  
  window.addEventListener('ws-close', handler);
  
  // 返回取消监听的函数
  return () => {
    window.removeEventListener('ws-close', handler);
  };
}

export default {
  initWebSocket,
  sendToServer,
  closeWebSocket,
  getWebSocketState,
  isConnected,
  onMessage,
  onClose
};
