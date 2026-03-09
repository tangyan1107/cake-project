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
 * 分类管理API
 */
export const categoryApi = {
  /**
   * 分类分页查询
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页记录数
   * @param {string} params.name - 分类名称
   * @param {number} params.type - 分类类型：1为菜品分类，2为套餐分类
   */
  getCategoryPage(params) {
    return request({
      url: '/admin/category/page',
      method: 'GET',
      params
    })
  },

  /**
   * 根据ID查询分类
   * @param {number} id - 分类ID
   */
  getCategoryById(id) {
    return request({
      url: `/admin/category/${id}`,
      method: 'GET'
    })
  },

  /**
   * 新增分类
   * @param {Object} data - 分类信息
   * @param {string} data.name - 分类名称
   * @param {number} data.sort - 排序
   * @param {number} data.type - 分类类型
   */
  addCategory(data) {
    return request({
      url: '/admin/category',
      method: 'POST',
      data
    })
  },

  /**
   * 修改分类
   * @param {Object} data - 分类信息
   * @param {number} data.id - 分类id
   * @param {string} data.name - 分类名称
   * @param {number} data.sort - 排序
   * @param {number} data.type - 分类类型
   */
  updateCategory(data) {
    return request({
      url: '/admin/category',
      method: 'PUT',
      data
    })
  },

  /**
   * 启用/禁用分类
   * @param {number} status - 状态 1:启用 0:禁用
   * @param {number} id - 分类 ID
   */
  updateCategoryStatus(status, id) {
    return request({
      url: `/admin/category/status/${status}`,
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: {
        id
      }
    })
  },

  /**
   * 根据类型查询分类
   * @param {number} type - 分类类型：1 为菜品分类，2 为套餐分类
   */
  getCategoryByType(type) {
    return request({
      url: '/admin/category/list',
      method: 'GET',
      params: { type }
    })
  },

  /**
   * 删除分类
   * @param {number} id - 分类 id
   */
  deleteCategory(id) {
    return request({
      url: `/admin/category`,
      method: 'DELETE',
      params: { 
        id: id
      }
    })
  }
}

export default request
