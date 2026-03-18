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
 * 订单管理API
 */
export const orderApi = {
  /**
   * 动态条件分页查询订单
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页记录数
   * @param {string} params.beginTime - 开始时间
   * @param {string} params.endTime - 结束时间
   * @param {string} params.number - 订单号
   * @param {string} params.phone - 手机号
   * @param {number} params.status - 订单状态
   */
  getOrderList(params) {
    return request({
      url: '/admin/order/conditionSearch',
      method: 'GET',
      params
    })
  },

  /**
   * 订单状态统计
   */
  getOrderStatistics() {
    return request({
      url: '/admin/order/statistics',
      method: 'GET'
    })
  },

  /**
   * 接单
   * @param {Object} data - 请求参数
   * @param {number} data.id - 订单 id
   */
  confirmOrder(data) {
    return request({
      url: '/admin/order/confirm',
      method: 'PUT',
      data
    })
  },

  /**
   * 拒单
   * @param {Object} data - 请求参数
   * @param {number} data.id - 订单 id
   * @param {string} data.rejectionReason - 拒单原因
   */
  rejectionOrder(data) {
    return request({
      url: '/admin/order/rejection',
      method: 'PUT',
      data
    })
  },

  /**
   * 取消订单
   * @param {Object} data - 请求参数
   * @param {number} data.id - 订单 id
   * @param {string} data.cancelReason - 取消原因
   */
  cancelOrder(data) {
    return request({
      url: '/admin/order/cancel',
      method: 'PUT',
      data
    })
  },

  /**
   * 派送订单
   * @param {number} id - 订单 id
   */
  deliveryOrder(id) {
    return request({
      url: `/admin/order/delivery/${id}`,
      method: 'PUT'
    })
  },

  /**
   * 获取订单统计数据
   */
  getOrderStatisticsData() {
    return request({
      url: '/admin/order/statistics',
      method: 'GET'
    })
  },

  /**
   * 完成订单
   * @param {number} id - 订单 id
   */
  completeOrder(id) {
    return request({
      url: `/admin/order/complete/${id}`,
      method: 'PUT'
    })
  }
}

export default request
