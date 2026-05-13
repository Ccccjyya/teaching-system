<template>
  <div class="dashboard-container">
    <div class="hero-card hero-admin">
      <div class="hero-left">
        <p class="hero-subtitle">ADMIN CONTROL CENTER</p>
        <h1>管理中心</h1>
        <p class="hero-welcome">欢迎回来，管理员</p>
        <div class="hero-tags">
          <el-tag size="mini" type="success">{{ currentSemester.semesterDesc || '学期未设置' }}</el-tag>
          <el-tag size="mini" type="info">{{ currentTime }}</el-tag>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-badge">
          <i class="el-icon-setting" />
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="mb16">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card s-student" shadow="hover" @click.native="goTo('/management/student')">
          <div class="stat-icon"><i class="el-icon-user" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.studentCount || 0 }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card s-teacher" shadow="hover" @click.native="goTo('/management/eduTeacher')">
          <div class="stat-icon"><i class="el-icon-user-solid" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.teacherCount || 0 }}</div>
            <div class="stat-label">教师总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card s-course" shadow="hover" @click.native="goTo('/management/eduCourse')">
          <div class="stat-icon"><i class="el-icon-folder" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.courseCount || 0 }}</div>
            <div class="stat-label">课程总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card s-apply" shadow="hover" @click.native="goTo('/management/eduApply')">
          <div class="stat-icon"><i class="el-icon-document" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.applyCount || 0 }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb16">
      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-header">快捷操作</div>
          <div class="quick-grid">
            <div class="quick-item" v-for="action in quickActions" :key="action.path" @click="goTo(action.path)">
              <div class="quick-icon" :class="action.colorClass"><i :class="action.iconClass" /></div>
              <div class="quick-name">{{ action.name }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="panel-card semester-card" shadow="never">
          <div slot="header" class="panel-header">当前学期</div>
          <div class="semester-main">{{ currentSemester.semesterDesc || '暂无数据' }}</div>
          <div class="semester-value">{{ currentSemester.semesterValue || '' }}</div>
          <div class="semester-actions">
            <el-button size="mini" type="primary" @click="goTo('/management/eduGlobal')">进入全局控制</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getStats } from '@/api/edu/dashboard'
import { getCurrentSemester } from '@/api/edu/semester'

export default {
  name: 'AdminIndex',
  data() {
    return {
      currentTime: '',
      stats: {
        studentCount: 0,
        teacherCount: 0,
        courseCount: 0,
        applyCount: 0
      },
      currentSemester: { semesterValue: '', semesterDesc: '' },
      quickActions: [
        { name: '学生管理', iconClass: 'el-icon-user', colorClass: 'c-blue', path: '/management/student' },
        { name: '教师管理', iconClass: 'el-icon-user-solid', colorClass: 'c-green', path: '/management/eduTeacher' },
        { name: '课程管理', iconClass: 'el-icon-folder', colorClass: 'c-cyan', path: '/management/eduCourse' },
        { name: '开课审核', iconClass: 'el-icon-document-checked', colorClass: 'c-orange', path: '/management/eduApply' },
        { name: '院系管理', iconClass: 'el-icon-office-building', colorClass: 'c-purple', path: '/management/department' },
        { name: '全局控制', iconClass: 'el-icon-set-up', colorClass: 'c-red', path: '/management/eduGlobal' }
      ],
      clockTimer: null
    }
  },
  mounted() {
    this.updateTime()
    this.clockTimer = setInterval(this.updateTime, 1000)
    this.loadData()
  },
  beforeDestroy() {
    if (this.clockTimer) {
      clearInterval(this.clockTimer)
      this.clockTimer = null
    }
  },
  methods: {
    normalizePath(path) {
      return (path || '').replace(/^\/+|\/+$/g, '')
    },
    joinPath(parent, child) {
      if (!child) return parent || ''
      if (child.startsWith('/')) return child
      const base = parent ? parent.replace(/\/+$/g, '') : ''
      return `${base}/${child}`
    },
    resolvePathByLeaf(leafPath) {
      const targetLeaf = this.normalizePath(leafPath)
      const routes = this.$store.getters.sidebarRouters || []
      const walk = (items, basePath) => {
        for (const item of items || []) {
          const currentPath = this.joinPath(basePath, item.path || '')
          if (this.normalizePath(item.path) === targetLeaf) {
            return currentPath.startsWith('/') ? currentPath : `/${currentPath}`
          }
          const hit = walk(item.children || [], currentPath)
          if (hit) return hit
        }
        return ''
      }
      return walk(routes, '')
    },
    updateTime() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      const second = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    goTo(path) {
      if (path && path.startsWith('/')) {
        this.$router.push(path).catch(() => {})
        return
      }
      const candidates = [path]
      if (path === 'operlog') {
        candidates.push('logininfor', 'log')
      }
      const target = candidates.map(item => this.resolvePathByLeaf(item)).find(Boolean)
      if (!target) {
        this.$message.error('目标页面未配置到当前账号菜单，请联系管理员授权菜单后重试')
        return
      }
      this.$router.push(target).catch(() => {})
    },
    loadData() {
      getStats().then(response => {
        this.stats = response.data || this.stats
      }).catch(() => {
        this.stats = { studentCount: 0, teacherCount: 0, courseCount: 0, applyCount: 0 }
      })

      getCurrentSemester().then(response => {
        this.currentSemester = response.data || this.currentSemester
      }).catch(() => {
        this.currentSemester = { semesterValue: '', semesterDesc: '暂无数据' }
      })
    }
  }
}
</script>

<style scoped>
.dashboard-container { padding: 20px; }
.mb16 { margin-bottom: 16px; }

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px;
  border-radius: 14px;
  margin-bottom: 16px;
  color: #fff;
}

.hero-admin { background: linear-gradient(135deg, #0f2f52 0%, #1b4d70 48%, #2d6a8d 100%); }

.hero-subtitle { margin: 0 0 6px; font-size: 12px; letter-spacing: 1px; opacity: .85; }
.hero-card h1 { margin: 0; font-size: 26px; font-weight: 700; }
.hero-welcome { margin: 8px 0 10px; font-size: 14px; opacity: .95; }
.hero-tags .el-tag + .el-tag { margin-left: 8px; }
.hero-badge {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255,255,255,.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.stat-card { border-radius: 12px; cursor: pointer; }
.stat-card ::v-deep .el-card__body { display: flex; align-items: center; padding: 18px; }
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  color: #fff;
  font-size: 24px;
}
.s-student .stat-icon { background: linear-gradient(135deg, #4361ee, #4cc9f0); }
.s-teacher .stat-icon { background: linear-gradient(135deg, #2ec4b6, #43aa8b); }
.s-course .stat-icon { background: linear-gradient(135deg, #4895ef, #00b4d8); }
.s-apply .stat-icon { background: linear-gradient(135deg, #ff7f11, #ffb703); }
.stat-value { font-size: 30px; line-height: 1; color: #303133; font-weight: 700; }
.stat-label { margin-top: 8px; font-size: 13px; color: #909399; }

.panel-card { border-radius: 12px; }
.panel-header { font-weight: 600; color: #303133; }

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(110px, 1fr));
  gap: 12px;
}
.quick-item {
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 14px 10px;
  text-align: center;
  cursor: pointer;
  transition: all .2s ease;
}
.quick-item:hover { transform: translateY(-2px); box-shadow: 0 8px 16px rgba(0,0,0,.08); }
.quick-icon {
  width: 42px;
  height: 42px;
  margin: 0 auto 8px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}
.c-blue { background: #3a86ff; }
.c-green { background: #43aa8b; }
.c-cyan { background: #00b4d8; }
.c-orange { background: #fb8500; }
.c-purple { background: #7b2cbf; }
.c-red { background: #e63946; }
.quick-name { font-size: 13px; color: #606266; }

.semester-card { min-height: 190px; }
.semester-main { font-size: 20px; color: #1b4d70; font-weight: 700; line-height: 1.4; }
.semester-value { margin-top: 6px; font-size: 13px; color: #909399; }
.semester-actions { margin-top: 12px; }

@media (max-width: 992px) {
  .hero-card { flex-direction: column; align-items: flex-start; gap: 12px; }
  .quick-grid { grid-template-columns: repeat(2, minmax(110px, 1fr)); }
}

@media (max-width: 768px) {
  .dashboard-container { padding: 12px; }
}
</style>
