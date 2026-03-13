import axios from 'axios'

const BASE_URL = 'http://localhost:8080'

// 创建axios实例
const request = axios.create({
  baseURL: BASE_URL,
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token 并添加到请求头
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
 * 通用API
 */
export const commonApi = {
  /**
   * 文件上传
   * @param {File} file - 要上传的文件对象
   * @returns {Promise} 返回文件上传后的URL
   */
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
      url: '/admin/common/upload',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default request
