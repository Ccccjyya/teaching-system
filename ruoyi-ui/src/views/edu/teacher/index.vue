<template>
  <div class="dashboard-container">
    <div class="hero-card">
      <div class="hero-left">
        <p class="hero-subtitle">TEACHER WORKBENCH</p>
        <h1>教师中心</h1>
        <p class="hero-welcome">欢迎回来，{{ username }}</p>
        <div class="hero-tags">
          <el-tag size="mini" type="success">{{ stats.currentSemester || '当前学期未设置' }}</el-tag>
          <el-tag size="mini" type="info">{{ currentTime }}</el-tag>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-badge">
          <i class="el-icon-reading" />
        </div>
      </div>
    </div>

    <el-row :gutter="16" class="mb16">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card stat-course" shadow="hover" @click.native="goTo('/teacherCourses/current')">
          <div class="stat-icon"><i class="el-icon-folder" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.courseCount || 0 }}</div>
            <div class="stat-label">本学期授课门数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card stat-student" shadow="hover" @click.native="goTo('/teacherCourses/current')">
          <div class="stat-icon"><i class="el-icon-user" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.studentCount || 0 }}</div>
            <div class="stat-label">覆盖学生人数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="stat-card stat-score" shadow="hover" @click.native="goTo('/teacherCourses/current')">
          <div class="stat-icon"><i class="el-icon-edit-outline" /></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingScoreCount || 0 }}</div>
            <div class="stat-label">待登分课程数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb16">
      <el-col :xs="24" :lg="16">
        <el-card class="panel-card" shadow="never">
          <div slot="header" class="panel-header">
            <span>快捷操作</span>
          </div>
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
          <div slot="header" class="panel-header">
            <span>当前学期</span>
          </div>
          <div class="semester-main">{{ stats.currentSemester || '暂无数据' }}</div>
          <div class="semester-tip">切换学期后，统计与课程同步更新</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <div slot="header" class="panel-header">
        <span>近期授课安排</span>
      </div>
      <div v-if="recentCourses.length" class="course-list">
        <div class="course-item" v-for="course in recentCourses" :key="course.offeringId">
          <div class="course-title-row">
            <div class="course-title">{{ course.courseName }}</div>
            <el-tag size="mini" type="warning">{{ course.courseNo }}</el-tag>
          </div>
          <div class="course-meta">{{ course.schedule || '未设置上课时间' }}</div>
          <div class="course-meta">地点：{{ course.location || '-' }} · 选课：{{ course.selectedCount || 0 }}/{{ course.maxCapacity || 0 }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无近期授课安排" />
    </el-card>
  </div>
</template>

<script>
import { getTeacherStats, getTeacherCourses } from '@/api/edu/teacher'

export default {
  name: 'TeacherIndex',
  data() {
    return {
      username: '',
      currentTime: '',
      timer: null,
      stats: {
        courseCount: 0,
        studentCount: 0,
        pendingScoreCount: 0,
        currentSemester: ''
      },
      recentCourses: [],
      quickActions: [
        { name: '当前学期授课', path: '/teacherCourses/current', iconClass: 'el-icon-notebook-2', colorClass: 'c-blue' },
        { name: '历史学期授课', path: '/teacherCourses/history', iconClass: 'el-icon-time', colorClass: 'c-teal' },
        { name: '开课申请', path: '/teacherApply/index', iconClass: 'el-icon-edit', colorClass: 'c-orange' }
      ]
    }
  },
  mounted() {
    this.username = this.$store.getters.nickName || this.$store.getters.name || '教师'
    this.updateClock()
    this.timer = setInterval(this.updateClock, 1000)
    this.loadData()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
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
    goTo(leafPath) {
      if (leafPath && leafPath.startsWith('/')) {
        this.$router.push(leafPath).catch(() => {})
        return
      }
      const target = this.resolvePathByLeaf(leafPath)
      if (!target) {
        this.$message.error('目标页面未配置到当前账号菜单，请联系管理员授权菜单后重试')
        return
      }
      this.$router.push(target).catch(() => {})
    },
    updateClock() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      const second = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    async loadData() {
      try {
        const [statsRes, coursesRes] = await Promise.all([getTeacherStats(), getTeacherCourses()])
        if (statsRes.code === 200) {
          this.stats = statsRes.data || this.stats
        }
        const rows = coursesRes.rows || coursesRes.data || []
        this.recentCourses = rows.slice(0, 6)
      } catch (e) {
        this.$message.error('教师首页数据加载失败')
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.mb16 {
  margin-bottom: 16px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px;
  border-radius: 14px;
  margin-bottom: 16px;
  color: #fff;
  background: linear-gradient(135deg, #1f4f78 0%, #1f7a8c 46%, #4ea8de 100%);
}

.hero-subtitle {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 1px;
  opacity: 0.85;
}

.hero-card h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
}

.hero-welcome {
  margin: 8px 0 10px;
  font-size: 14px;
  opacity: 0.95;
}

.hero-tags .el-tag + .el-tag {
  margin-left: 8px;
}

.hero-badge {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.stat-card {
  border-radius: 12px;
  cursor: pointer;
}

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
.stat-student .stat-icon { background: linear-gradient(135deg, #2ec4b6, #52b788); }
.stat-score .stat-icon { background: linear-gradient(135deg, #ff7f11, #ffb703); }

.stat-value {
  font-size: 30px;
  line-height: 1;
  color: #303133;
  font-weight: 700;
}

.stat-label {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.panel-card {
  border-radius: 12px;
}

.panel-header {
  font-weight: 600;
  color: #303133;
}

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
  transition: all 0.2s ease;
}

.quick-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

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
.c-teal { background: #2ec4b6; }
.c-orange { background: #fb8500; }

.quick-name {
  font-size: 13px;
  color: #606266;
}

.semester-card {
  min-height: 166px;
}

.semester-main {
  font-size: 22px;
  color: #1f4f78;
  font-weight: 700;
  line-height: 1.4;
}

.semester-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.course-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
}

.course-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.course-title {
  font-size: 15px;
  color: #303133;
  font-weight: 600;
}

.course-meta {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .course-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(110px, 1fr));
  }
}
</style>
