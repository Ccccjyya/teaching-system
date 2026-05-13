import auth from '@/plugins/auth'
import store from '@/store'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

const studentHomeRoute = {
  path: '/',
  component: Layout,
  children: [
    {
      path: 'studentIndex',
      component: () => import('@/views/dashboard/studentIndex'),
      name: 'StudentIndex',
      meta: { title: '学生首页', icon: 'dashboard', affix: true }
    }
  ]
}

const teacherHomeRoute = {
  path: '/',
  component: Layout,
  children: [
    {
      path: 'teacherIndex',
      component: () => import('@/views/edu/teacher/index'),
      name: 'TeacherIndex',
      meta: { title: '教师首页', icon: 'education', affix: true }
    }
  ]
}

const teacherCoursesRoute = {
  path: '/teacherCourses',
  component: Layout,
  alwaysShow: true,
  redirect: 'noRedirect',
  meta: { title: '我的授课', icon: 'education' },
  children: [
    {
      path: 'current',
      component: () => import('@/views/edu/teacher/courses/current'),
      name: 'TeacherCurrentCourses',
      meta: { title: '当前学期授课', icon: 'education' }
    },
    {
      path: 'history',
      component: () => import('@/views/edu/teacher/courses/history'),
      name: 'TeacherHistoryCourses',
      meta: { title: '历史学期授课', icon: 'education' }
    }
  ]
}

const teacherApplyRoute = {
  path: '/teacherApply',
  component: Layout,
  children: [
    {
      path: 'index',
      component: () => import('@/views/edu/teacher/apply/index'),
      name: 'TeacherApply',
      meta: { title: '开课申请', icon: 'edit' }
    }
  ]
}

const adminHomeChildPath = 'dashboard'
const studentHomeChildPath = 'studentIndex'
const teacherHomeChildPath = 'teacherIndex'
const studentOnlyLeafPaths = new Set([
  'studentIndex',
  'courseOffering',
  'myCourses',
  'grades',
  'courseTable',
  'course'
])
const teacherOnlyLeafPaths = new Set([
  'teacherIndex',
  'teacher',
  'teacherHome',
  'teacherCourses',
  'teacherCurrentCourses',
  'teacherHistoryCourses',
  'current',
  'history',
  'teacherApply',
  'apply'
])
const nonAdminLeafPaths = new Set([
  ...studentOnlyLeafPaths,
  ...teacherOnlyLeafPaths
])

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      // defaultRoutes 用于顶部菜单/部分布局切换时的侧边栏数据源，不应强制拼接 constantRoutes。
      // 否则会把“管理首页(dashboard)”等常量路由带到学生端菜单里。
      state.defaultRoutes = routes
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      state.topbarRouters = routes
    },
    SET_SIDEBAR_ROUTERS: (state, routes) => {
      state.sidebarRouters = routes
    },
  },
  actions: {
    // 生成路由
    GenerateRoutes({ commit }) {
      return new Promise(resolve => {
        // 向后端请求路由数据
        getRouters().then(res => {
          const sdata = JSON.parse(JSON.stringify(res.data))
          const rdata = JSON.parse(JSON.stringify(res.data))
          const sidebarRoutes = filterAsyncRouter(sdata)
          const rewriteRoutes = filterAsyncRouter(rdata, false, true).filter(route => !hasRoutePath([route], 'studentIndex'))
          const asyncRoutes = filterDynamicRoutes(dynamicRoutes)
          const visibleSidebarRoutes = buildRoleSidebarRoutes(sidebarRoutes)
          const visibleDefaultRoutes = buildRoleDefaultRoutes(sidebarRoutes, visibleSidebarRoutes)
          const accessibleRewriteRoutes = buildAccessibleRoutes(rewriteRoutes)
          rewriteRoutes.push({ path: '*', redirect: '/404', hidden: true })
          router.addRoutes(asyncRoutes)
          commit('SET_ROUTES', accessibleRewriteRoutes.concat([{ path: '*', redirect: '/404', hidden: true }]))
          commit('SET_SIDEBAR_ROUTERS', visibleSidebarRoutes)
          commit('SET_DEFAULT_ROUTES', visibleDefaultRoutes)
          commit('SET_TOPBAR_ROUTES', visibleDefaultRoutes)
          resolve(accessibleRewriteRoutes.concat([{ path: '*', redirect: '/404', hidden: true }]))
        })
      })
    }
  }
}

function buildRoleSidebarRoutes(sidebarRoutes) {
  if (auth.hasRole('admin')) {
    const adminRoutes = filterOutLeafPaths(constantRoutes.concat(sidebarRoutes), nonAdminLeafPaths)
    return dedupeRoutesByFullPath(removeHomeByType(removeHomeByType(adminRoutes, 'student'), 'teacher'))
  }
  if (auth.hasRole('student') || store.getters.identity === 'student') {
    const studentRoutes = filterRoutesByLeafPaths(sidebarRoutes, studentOnlyLeafPaths)
    if (!hasRoutePath(studentRoutes, 'studentIndex')) {
      studentRoutes.unshift(studentHomeRoute)
    }
    return removeHomeByType(studentRoutes, 'admin')
  }
  if (isTeacherIdentity()) {
    const teacherRoutes = filterRoutesByLeafPaths(sidebarRoutes, teacherOnlyLeafPaths)
    if (teacherRoutes.length > 0) {
      return ensureTeacherRoutes(teacherRoutes)
    }
    if (hasRoutePath(sidebarRoutes, 'teacher') || hasRoutePath(sidebarRoutes, 'teacherCourses')) {
      return ensureTeacherRoutes(sidebarRoutes)
    }
    return ensureTeacherRoutes([])
  }
  return dedupeRoutesByFullPath(removeHomeByType(constantRoutes.concat(sidebarRoutes), 'student'))
}

function buildRoleDefaultRoutes(sidebarRoutes, visibleSidebarRoutes) {
  if (auth.hasRole('admin')) {
    const adminRoutes = filterOutLeafPaths(constantRoutes.concat(sidebarRoutes), nonAdminLeafPaths)
    return dedupeRoutesByFullPath(removeHomeByType(removeHomeByType(adminRoutes, 'student'), 'teacher'))
  }
  if (isTeacherIdentity()) {
    return visibleSidebarRoutes
  }
  if (store.getters.identity === 'student') {
    return visibleSidebarRoutes
  }
  return removeHomeByType(visibleSidebarRoutes, 'admin')
}

function isTeacherIdentity() {
  const roles = store.getters && store.getters.roles ? store.getters.roles : []
  const isAdmin = roles.includes('admin')
  if (isAdmin) {
    return false
  }
  return roles.includes('teacher') || store.getters.identity === 'teacher'
}

function ensureTeacherRoutes(routes) {
  const nextRoutes = [...(routes || [])]
  if (!hasRoutePath(nextRoutes, 'teacherIndex') && !hasRoutePath(nextRoutes, 'teacher')) {
    nextRoutes.unshift(teacherHomeRoute)
  }
  if (!hasRoutePath(nextRoutes, 'teacherCourses')) {
    nextRoutes.push(teacherCoursesRoute)
  }
  if (!hasRoutePath(nextRoutes, 'teacherApply') && !hasRoutePath(nextRoutes, 'apply')) {
    nextRoutes.push(teacherApplyRoute)
  }
  return nextRoutes
}

function buildAccessibleRoutes(rewriteRoutes) {
  if (!isTeacherIdentity()) {
    return rewriteRoutes
  }
  return ensureTeacherRoutes(rewriteRoutes)
}

function isRoutePath(route, path) {
  return normalizeRoutePath(route.path) === path
}

function normalizeRoutePath(path) {
  return (path || '').replace(/^\/+/, '')
}

function hasRoutePath(routes, path) {
  return routes.some(route => {
    if (normalizeRoutePath(route.path) === path) {
      return true
    }
    return route.children && hasRoutePath(route.children, path)
  })
}

function routeHasHomeChild(route, homePath) {
  if (!route || !route.children || !route.children.length) {
    return false
  }
  return route.children.some(child => normalizeRoutePath(child.path) === homePath)
}

function isHomeRouteType(route, type) {
  const normalizedPath = normalizeRoutePath(route.path)
  if (type === 'admin') {
    return normalizedPath === adminHomeChildPath || routeHasHomeChild(route, adminHomeChildPath)
  }
  if (type === 'teacher') {
    return normalizedPath === teacherHomeChildPath || routeHasHomeChild(route, teacherHomeChildPath)
  }
  return normalizedPath === studentHomeChildPath || routeHasHomeChild(route, studentHomeChildPath)
}

function removeHomeByType(routes, type) {
  return (routes || []).filter(route => !isHomeRouteType(route, type))
}

/**
 * 根据允许的叶子节点 path 过滤菜单树：
 * - 叶子节点：path 在 allowedLeafPaths 中才保留
 * - 目录节点：只要其子节点过滤后仍有内容就保留
 */
function filterRoutesByLeafPaths(routes, allowedLeafPaths) {
  return (routes || []).map(route => {
    const next = { ...route }
    if (next.children && next.children.length) {
      next.children = filterRoutesByLeafPaths(next.children, allowedLeafPaths)
      if (next.children.length) {
        return next
      }
      delete next['children']
      delete next['redirect']
    }
    return allowedLeafPaths.has(normalizeRoutePath(next.path)) ? next : null
  }).filter(Boolean)
}

/**
 * 从菜单树中剔除指定叶子 path（会自动清理空目录）
 */
function filterOutLeafPaths(routes, excludedLeafPaths) {
  return (routes || []).map(route => {
    const next = { ...route }
    if (next.children && next.children.length) {
      next.children = filterOutLeafPaths(next.children, excludedLeafPaths)
      if (next.children.length) {
        return next
      }
      delete next['children']
      delete next['redirect']
    }
    return excludedLeafPaths.has(normalizeRoutePath(next.path)) ? null : next
  }).filter(Boolean)
}

/**
 * 按完整路径去重，避免常量路由与后端菜单重复（如 dashboard）
 */
function dedupeRoutesByFullPath(routes, basePath = '') {
  const seen = new Set()
  const result = []
  ;(routes || []).forEach(route => {
    const next = { ...route }
    const currentPath = joinRoutePath(basePath, next.path || '')
    if (next.children && next.children.length) {
      next.children = dedupeRoutesByFullPath(next.children, currentPath)
      if (!next.children.length) {
        delete next['children']
        delete next['redirect']
      }
    }
    const key = normalizeRoutePath(currentPath)
    if (!seen.has(key)) {
      seen.add(key)
      result.push(next)
    }
  })
  return result
}

function joinRoutePath(parent, child) {
  if (!child) return parent || ''
  if (child.startsWith('/')) return child
  const base = parent ? parent.replace(/\/+$/g, '') : ''
  return `${base}/${child}`
}

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes) {
  const res = []
  routes.forEach(route => {
    if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else if (route.roles) {
      if (auth.hasRoleOr(route.roles)) {
        res.push(route)
      }
    }
  })
  return res
}

export const loadView = (view) => {
  if (process.env.NODE_ENV === 'development') {
    return (resolve) => require([`@/views/${view}`], resolve)
  } else {
    // 使用 import 实现生产环境的路由懒加载
    return () => import(`@/views/${view}`)
  }
}

export default permission
