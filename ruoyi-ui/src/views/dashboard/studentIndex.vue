<template>
  <div class="dashboard-container">
    <div class="hero-card hero-student">
      <div class="hero-left">
        <p class="hero-subtitle">STUDENT WORKBENCH</p>
        <h1>学生中心</h1>
        <p class="hero-welcome">欢迎回来，{{ username }}</p>
        <div class="hero-tags">
          <el-tag size="mini" type="success">{{ currentSemester || '当前学期未设置' }}</el-tag>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-badge">
          <i class="el-icon-notebook-2" />
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="mb16">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card stat-course" shadow="hover" @click.native="goTo('/edu/course/myCourses', 'myCourses')">
          <div class="stat-icon"><i class="el-icon-reading" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ courseCount || 0 }}</div>
            <div class="stat-label">已选课程</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card stat-credit" shadow="hover" @click.native="goTo('/edu/course/grades', 'grades')">
          <div class="stat-icon"><i class="el-icon-medal" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ creditCount || 0 }}</div>
            <div class="stat-label">已修学分</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card stat-gpa" shadow="hover" @click.native="goTo('/edu/course/grades', 'grades')">
          <div class="stat-icon"><i class="el-icon-data-analysis" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ formatGpa(totalGpa) }}</div>
            <div class="stat-label">总 GPA</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16" class="mb16">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-header">快捷操作</div>
          <div class="quick-grid">
            <div class="quick-item" v-for="action in quickActions" :key="action.path" @click="goTo(action.path, action.leafPath)">
              <div class="quick-icon" :class="action.colorClass"><i :class="action.iconClass" /></div>
              <div class="quick-name">{{ action.name }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8" class="mb16">
        <el-card class="panel-card semester-card" shadow="never">
          <div slot="header" class="panel-header">当前学期</div>
          <div class="overview-main">{{ currentSemester || '暂无数据' }}</div>
          <div class="overview-sub">本学期请关注选课、成绩与课表更新</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getCurrentSemester } from '@/api/edu/semester'
import { getStudentStats } from '@/api/edu/student'

export default {
  name: 'StudentIndex',
  data() {
    return {
      username: '',
      courseCount: 0,
      creditCount: 0,
      totalGpa: 0,
      currentSemester: '',
      quickActions: [
        { name: '选课管理', path: '/edu/course/courseOffering', leafPath: 'courseOffering', iconClass: 'el-icon-collection', colorClass: 'c-blue' },
        { name: '我的课程', path: '/edu/course/myCourses', leafPath: 'myCourses', iconClass: 'el-icon-folder-opened', colorClass: 'c-green' },
        { name: '成绩查询', path: '/edu/course/grades', leafPath: 'grades', iconClass: 'el-icon-data-analysis', colorClass: 'c-orange' },
        { name: '我的课表', path: '/edu/course/courseTable', leafPath: 'courseTable', iconClass: 'el-icon-date', colorClass: 'c-red' }
      ]
    }
  },
  mounted() {
    this.loadData()
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
    goTo(path, leafPath) {
      const fallbackLeafPath = leafPath || path
      const resolvedTarget = this.resolvePathByLeaf(fallbackLeafPath)
      if (resolvedTarget) {
        this.$router.push(resolvedTarget).then(() => {
          if (this.$route && this.$route.path === '/404') {
            this.$message.error('目标页面未配置到当前账号菜单，请联系管理员授权菜单后重试')
            this.$router.replace('/studentIndex').catch(() => {})
          }
        }).catch(() => {})
        return
      }
      if (path && path.startsWith('/')) {
        this.$router.push(path).then(() => {
          if (this.$route && this.$route.path === '/404') {
            this.$message.error('目标页面未配置到当前账号菜单，请联系管理员授权菜单后重试')
            this.$router.replace('/studentIndex').catch(() => {})
          }
        }).catch(() => {})
        return
      }
      if (!resolvedTarget) {
        this.$message.error('目标页面未配置到当前账号菜单，请联系管理员授权菜单后重试')
        return
      }
      this.$router.push(resolvedTarget).catch(() => {})
    },
    loadData() {
      this.username = this.$store.getters.nickName || this.$store.getters.name || '学生'
      Promise.all([getCurrentSemester(), getStudentStats()]).then(([semesterResponse, statsResponse]) => {
        const semester = semesterResponse.data || {}
        this.currentSemester = semester.semesterDesc || semester.semesterValue || ''
        const stats = statsResponse.data || {}
        this.courseCount = stats.courseCount || 0
        this.creditCount = stats.creditCount || 0
        this.totalGpa = stats.totalGpa || 0
      }).catch(() => {
        this.currentSemester = ''
      })
    },
    formatGpa(value) {
      const num = Number(value)
      if (Number.isNaN(num)) return '0.00'
      return num.toFixed(2)
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

.hero-student {
  background: linear-gradient(135deg, #4b3f72 0%, #2f5d8a 48%, #3d84b8 100%);
}

.hero-subtitle { margin: 0 0 6px; font-size: 12px; letter-spacing: 1px; opacity: .85; }
.hero-card h1 { margin: 0; font-size: 26px; font-weight: 700; }
.hero-welcome { margin: 8px 0 10px; font-size: 14px; opacity: .95; }
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
.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 18px;
}
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
.stat-course .stat-icon { background: linear-gradient(135deg, #3a86ff, #4cc9f0); }
.stat-credit .stat-icon { background: linear-gradient(135deg, #ff7f11, #ffb703); }
.stat-gpa .stat-icon { background: linear-gradient(135deg, #2ec4b6, #43aa8b); }
.stat-value { font-size: 30px; line-height: 1; color: #303133; font-weight: 700; }
.stat-label { margin-top: 8px; font-size: 13px; color: #909399; }

.panel-card { border-radius: 12px; }
.panel-header { font-weight: 600; color: #303133; }

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
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
.c-orange { background: #fb8500; }
.c-red { background: #e63946; }
.quick-name { font-size: 13px; color: #606266; }

.semester-card { min-height: 180px; }
.overview-main { font-size: 22px; color: #2f5d8a; font-weight: 700; line-height: 1.4; }
.overview-sub { margin-top: 10px; font-size: 12px; color: #909399; }
@media (max-width: 992px) {
  .hero-card { flex-direction: column; align-items: flex-start; gap: 12px; }
}

@media (max-width: 768px) {
  .dashboard-container { padding: 12px; }
}
</style>
