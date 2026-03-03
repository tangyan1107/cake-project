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
 * 套餐管理API
 */
export const setmealApi = {
  /**
   * 套餐分页查询
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页记录数
   * @param {number} params.categoryId - 分类id
   * @param {string} params.name - 套餐名称
   * @param {number} params.status - 状态 1:启售 0:停售
   */
  getSetmealPage(params) {
    return request({
      url: '/admin/setmeal/page',
      method: 'GET',
      params
    })
  },

  /**
   * 新增套餐
   * @param {Object} data - 套餐信息
   */
  addSetmeal(data) {
    return request({
      url: '/admin/setmeal',
      method: 'POST',
      data
    })
  },

  /**
   * 根据ID查询套餐
   * @param {number} id - 套餐ID
   */
  getSetmealById(id) {
    return request({
      url: `/admin/setmeal/${id}`,
      method: 'GET'
    })
  },

  /**
   * 修改套餐
   * @param {Object} data - 套餐信息
   */
  updateSetmeal(data) {
    return request({
      url: '/admin/setmeal',
      method: 'PUT',
      data
    })
  },

  /**
   * 删除套餐
   * @param {string} ids - 套餐id，多个用逗号分隔
   */
  deleteSetmeal(ids) {
    return request({
      url: '/admin/setmeal',
      method: 'DELETE',
      params: { ids }
    })
  },

  /**
   * 启用/禁用套餐
   * @param {number} status - 状态 1:启售 0:停售
   * @param {number} id - 套餐ID
   */
  updateSetmealStatus(status, id) {
    return request({
      url: `/admin/setmeal/status/${status}/${id}`,
      method: 'PUT'
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
   * 根据分类id获取菜品列表
   * @param {number} categoryId - 分类id
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
