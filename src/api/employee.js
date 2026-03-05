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
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 员工管理API
 */
export const employeeApi = {
  /**
   * 员工登录
   * @param {Object} data - 登录信息
   * @param {string} data.username - 用户名
   * @param {string} data.password - 密码
   */
  login(data) {
    return request({
      url: '/admin/employee/login',
      method: 'POST',
      data
    })
  },

  /**
   * 分页查询员工
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页记录数
   * @param {string} params.name - 员工姓名
   */
  getEmployeePage(params) {
    return request({
      url: '/admin/employee/page',
      method: 'GET',
      params
    })
  },

  /**
   * 新增员工
   * @param {Object} data - 员工信息
   */
  addEmployee(data) {
    return request({
      url: '/admin/employee',
      method: 'POST',
      data
    })
  },

  /**
   * 根据ID查询员工
   * @param {number} id - 员工ID
   */
  getEmployeeById(id) {
    return request({
      url: `/admin/employee/${id}`,
      method: 'GET'
    })
  },

  /**
   * 修改员工
   * @param {Object} data - 员工信息
   */
  updateEmployee(data) {
    return request({
      url: '/admin/employee',
      method: 'PUT',
      data
    })
  },

  /**
   * 启用/禁用员工账号
   * @param {number} status - 状态 1:启用 0:禁用
   * @param {number} id - 员工ID
   */
  updateEmployeeStatus(status, id) {
    return request({
      url: `/admin/employee/status/${status}/${id}`,
      method: 'PUT'
    })
  },

  /**
   * 查询今日运营数据
   */
  getBusinessData() {
    return request({
      url: '/admin/workspace/businessData',
      method: 'GET'
    })
  },

  /**
   * 查询今日订单数据
   */
  getOverviewOrders() {
    return request({
      url: '/admin/workspace/overviewOrders',
      method: 'GET'
    })
  },

  /**
   * 查询菜品总览
   */
  getOverviewDishes() {
    return request({
      url: '/admin/workspace/overviewDishes',
      method: 'GET'
    })
  },

  /**
   * 查询套餐总览
   */
  getOverviewSetmeals() {
    return request({
      url: '/admin/workspace/overviewSetmeals',
      method: 'GET'
    })
  }
}

export default request
