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
    // 处理 401 未授权错误
    if (error.response && error.response.status === 401) {
      // 清除 token 并跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 数据统计API
 */
export const reportApi = {
  /**
   * 营业额统计
   * @param {Object} params - 查询参数
   * @param {string} params.begin - 开始日期
   * @param {string} params.end - 结束日期
   */
  getTurnoverStatistics(params) {
    return request({
      url: '/admin/report/turnoverStatistics',
      method: 'GET',
      params
    })
  },

  /**
   * 用户统计
   * @param {Object} params - 查询参数
   * @param {string} params.begin - 开始日期
   * @param {string} params.end - 结束日期
   */
  getUserStatistics(params) {
    return request({
      url: '/admin/report/userStatistics',
      method: 'GET',
      params
    })
  },

  /**
   * 订单统计
   * @param {Object} params - 查询参数
   * @param {string} params.begin - 开始日期
   * @param {string} params.end - 结束日期
   */
  getOrdersStatistics(params) {
    return request({
      url: '/admin/report/ordersStatistics',
      method: 'GET',
      params
    })
  },

  /**
   * 查询销量排名top10
   * @param {Object} params - 查询参数
   * @param {string} params.begin - 开始日期
   * @param {string} params.end - 结束日期
   */
  getTop10(params) {
    return request({
      url: '/admin/report/top10',
      method: 'GET',
      params
    })
  },

  /**
   * 导出Excel报表
   * @param {Object} params - 查询参数
   * @param {string} params.begin - 开始日期
   * @param {string} params.end - 结束日期
   */
  exportExcel(params) {
    return request({
      url: '/admin/report/export',
      method: 'GET',
      params,
      responseType: 'blob'
    })
  }
}

export default request
