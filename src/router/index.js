import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台' }
      },
      {
        path: '/order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: '/category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: '/dish',
        name: 'Dish',
        component: () => import('@/views/dish/index.vue'),
        meta: { title: '菜品管理' }
      },
      {
        path: '/setmeal',
        name: 'Setmeal',
        component: () => import('@/views/setmeal/index.vue'),
        meta: { title: '套餐管理' }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: '/employee',
        name: 'Employee',
        component: () => import('@/views/employee/index.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: '/employee/add',
        name: 'EmployeeAdd',
        component: () => import('@/views/employee/form.vue'),
        meta: { title: '添加员工' }
      },
      {
        path: '/employee/edit/:id',
        name: 'EmployeeEdit',
        component: () => import('@/views/employee/form.vue'),
        meta: { title: '修改员工' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 拦截未登录用户
router.beforeEach((to, from, next) => {
  // 获取 token
  const token = localStorage.getItem('token')
  
  // 如果访问的是登录页，直接放行
  if (to.path === '/login') {
    next()
    return
  }
  
  // 如果有 token，放行
  if (token) {
    next()
  } else {
    // 没有 token，重定向到登录页
    next('/login')
  }
})

export default router
