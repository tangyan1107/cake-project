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
 * 菜品管理API
 */
export const dishApi = {
  /**
   * 菜品分页查询
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页记录数
   * @param {number} params.categoryId - 分类id
   * @param {string} params.name - 菜品名称
   * @param {number} params.status - 状态 1:启售 0:停售
   */
  getDishPage(params) {
    return request({
      url: '/admin/dish/page',
      method: 'GET',
      params
    })
  },

  /**
   * 新增菜品
   * @param {Object} data - 菜品信息
   */
  addDish(data) {
    return request({
      url: '/admin/dish',
      method: 'POST',
      data
    })
  },

  /**
   * 根据ID查询菜品
   * @param {number} id - 菜品ID
   */
  getDishById(id) {
    return request({
      url: `/admin/dish/${id}`,
      method: 'GET'
    })
  },

  /**
   * 修改菜品
   * @param {Object} data - 菜品信息
   */
  updateDish(data) {
    return request({
      url: '/admin/dish',
      method: 'PUT',
      data
    })
  },

  /**
   * 删除菜品
   * @param {string} ids - 菜品id，多个用逗号分隔
   */
  deleteDish(ids) {
    return request({
      url: '/admin/dish',
      method: 'DELETE',
      params: { ids }
    })
  },

  /**
   * 启用/禁用菜品
   * @param {number} status - 状态 1:启售 0:停售
   * @param {number} id - 菜品 ID
   */
  updateDishStatus(status, id) {
    return request({
      url: `/admin/dish/status/${status}`,
      method: 'POST',
      params: { id }
    })
  },

  /**
   * 获取分类列表（用于下拉选择）
   */
  getCategoryList() {
    return request({
      url: '/admin/category/list',
      method: 'GET'
    })
  },

  /**
   * 根据分类 id 查询菜品
   * @param {number} categoryId - 分类 id
   */
  getDishListByCategoryId(categoryId) {
    return request({
      url: '/admin/dish/list',
      method: 'GET',
      params: { categoryId }
    })
  }
}

export default request
