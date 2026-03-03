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
  }
}

export default request
