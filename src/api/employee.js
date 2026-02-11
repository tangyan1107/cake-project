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
    // 可以在这里添加token等认证信息
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
 * 员工管理API
 */
export const employeeApi = {
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
  }
}

export default request
