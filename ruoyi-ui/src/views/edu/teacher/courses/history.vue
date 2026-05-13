<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="mb8">
      <el-form-item label="课程号">
        <el-input
          v-model="queryParams.courseNo"
          placeholder="请输入课程号"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="课程名">
        <el-input
          v-model="queryParams.courseName"
          placeholder="请输入课程名"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
    </el-row>

    <el-card class="box-card" v-if="total > 0">
      <el-table v-loading="loading" :data="currentPageData" border>
        <el-table-column label="学年学期" align="center" width="180">
          <template slot-scope="scope">
            {{ formatSemester(scope.row.semester) }}
          </template>
        </el-table-column>
        <el-table-column label="课程号" align="center" prop="courseNo" width="120" />
        <el-table-column label="课程名称" align="center" prop="courseName" />
        <el-table-column label="学分" align="center" prop="credit" width="80" />
        <el-table-column label="学时" align="center" prop="hours" width="80" />
        <el-table-column label="上课时间" align="center" prop="schedule" />
        <el-table-column label="选课人数" align="center" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.selectedCount }} / {{ scope.row.maxCapacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" icon="el-icon-user" @click="viewStudents(scope.row)">学生名单</el-button>
            <el-button size="mini" type="success" icon="el-icon-document" @click="viewScores(scope.row)">成绩查询</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > pageSize"
        :total="total"
        :page.sync="pageNum"
        :limit.sync="pageSize"
        @pagination="handlePageChange"
      />
    </el-card>
    <div v-else class="empty-info">
      <el-empty description="暂无历史学期授课信息" />
    </div>

    <el-dialog :title="(currentCourse && currentCourse.courseName) || '课程详情'" :visible.sync="open" width="900px" append-to-body>
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="学生名单" name="students">
          <el-form :model="studentQuery" ref="studentQueryForm" size="small" :inline="true" class="mb8">
            <el-form-item>
              <el-input
                v-model="studentQuery.keyword"
                placeholder="请输入学号或姓名搜索"
                clearable
                style="width: 300px"
                @keyup.enter.native="handleStudentQuery"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" size="mini" @click="handleStudentQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="resetStudentQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="studentLoading" :data="studentList" border>
            <el-table-column label="学号" align="center" prop="student.studentNo" width="120" />
            <el-table-column label="姓名" align="center" prop="student.studentName" width="100" />
            <el-table-column label="学院" align="center" width="150">
              <template slot-scope="scope">
                {{ scope.row.studentDeptName || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="手机号" align="center" width="120">
              <template slot-scope="scope">
                {{ scope.row.studentPhone || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="平时成绩" align="center" prop="usualScore" width="100" />
            <el-table-column label="考试成绩" align="center" prop="examScore" width="100" />
            <el-table-column label="总评成绩" align="center" prop="totalScore" width="100" />
          </el-table>
          <div slot="footer" class="dialog-footer">
            <el-button type="success" icon="el-icon-download" @click="exportStudents">导出名单</el-button>
            <el-button @click="cancel">关闭</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="成绩统计" name="statistics">
          <el-row :gutter="20" class="mb8">
            <el-col :span="6">
              <el-card>
                <div class="stat-value">{{ statistics.totalStudents }}</div>
                <div class="stat-label">总人数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card>
                <div class="stat-value">{{ statistics.averageScore }}</div>
                <div class="stat-label">平均分</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card>
                <div class="stat-value">{{ statistics.passRate }}%</div>
                <div class="stat-label">及格率</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card>
                <div class="stat-value">{{ statistics.excellentRate }}%</div>
                <div class="stat-label">优秀率</div>
              </el-card>
            </el-col>
          </el-row>
          <el-table v-loading="scoreLoading" :data="scoreList" border>
            <el-table-column label="学号" align="center" prop="student.studentNo" width="120" />
            <el-table-column label="姓名" align="center" prop="student.studentName" width="100" />
            <el-table-column label="平时成绩" align="center" prop="usualScore" width="100" />
            <el-table-column label="考试成绩" align="center" prop="examScore" width="100" />
            <el-table-column label="总评成绩" align="center" prop="totalScore" width="100" />
            <el-table-column label="绩点" align="center" width="80">
              <template slot-scope="scope">
                <span :class="getGradeClass(scope.row.totalScore)">{{ getGPA(scope.row.totalScore) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div slot="footer" class="dialog-footer">
            <el-button type="success" icon="el-icon-download" @click="exportScores">导出成绩</el-button>
            <el-button @click="cancel">关闭</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { getTeacherCourses } from '@/api/edu/teacher'
import { getCourseStudents } from '@/api/edu/enrollment'
import { getCurrentSemester } from '@/api/edu/semester'

export default {
  name: 'HistoryCourses',
  data() {
    return {
      loading: true,
      studentLoading: false,
      scoreLoading: false,
      allCourses: [],
      studentList: [],
      scoreList: [],
      total: 0,
      open: false,
      activeTab: 'students',
      currentCourse: null,
      currentSemester: '',
      queryParams: {
        courseNo: undefined,
        courseName: undefined
      },
      pageNum: 1,
      pageSize: 10,
      fetchParams: {
        pageNum: 1,
        pageSize: 100,
        courseNo: undefined,
        courseName: undefined
      },
      studentQuery: {
        keyword: undefined
      },
      statistics: {
        totalStudents: 0,
        averageScore: 0,
        passRate: 0,
        excellentRate: 0
      }
    }
  },
  computed: {
    filteredCourses() {
      return (this.allCourses || []).filter(course => !this.currentSemester || course.semester !== this.currentSemester)
    },
    currentPageData() {
      const start = (this.pageNum - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredCourses.slice(start, end)
    }
  },
  created() {
    this.loadCurrentSemester()
  },
  methods: {
    loadCurrentSemester() {
      getCurrentSemester().then(response => {
        this.currentSemester = response.data ? response.data.semesterValue : ''
        this.getList()
      })
    },
    formatSemester(semesterCode) {
      if (!semesterCode) return '-'
      const parts = semesterCode.split('-')
      if (parts.length >= 3) {
        const year = parts[0]
        const term = parts[2]
        const termName = term === '1' ? '秋季学期' : term === '2' ? '春季学期' : term
        return `${year}-${parseInt(year) + 1}学年 ${termName}`
      }
      return semesterCode
    },
    getGPA(score) {
      if (!score) return '-'
      const s = parseFloat(score)
      if (s >= 90) return '4.0'
      if (s >= 85) return '3.7'
      if (s >= 82) return '3.3'
      if (s >= 78) return '3.0'
      if (s >= 75) return '2.7'
      if (s >= 72) return '2.3'
      if (s >= 68) return '2.0'
      if (s >= 64) return '1.5'
      if (s >= 60) return '1.0'
      return '0'
    },
    getGradeClass(score) {
      if (!score) return ''
      const s = parseFloat(score)
      if (s >= 90) return 'grade-excellent'
      if (s >= 60) return 'grade-pass'
      return 'grade-fail'
    },
    calculateStatistics(scores) {
      const validScores = scores.filter(s => s.totalScore && !isNaN(s.totalScore)).map(s => parseFloat(s.totalScore))
      const total = validScores.length
      const avg = total > 0 ? Math.round(validScores.reduce((a, b) => a + b, 0) / total) : 0
      const passCount = validScores.filter(s => s >= 60).length
      const excellentCount = validScores.filter(s => s >= 90).length
      return {
        totalStudents: total,
        averageScore: avg,
        passRate: total > 0 ? Math.round((passCount / total) * 100) : 0,
        excellentRate: total > 0 ? Math.round((excellentCount / total) * 100) : 0
      }
    },
    getList() {
      this.loading = true
      this.fetchParams.courseNo = this.queryParams.courseNo
      this.fetchParams.courseName = this.queryParams.courseName
      getTeacherCourses(this.fetchParams).then(response => {
        this.allCourses = response.rows || []
        this.total = this.filteredCourses.length
        this.loading = false
      })
    },
    handleQuery() {
      this.pageNum = 1
      this.getList()
    },
    handlePageChange({ page, limit }) {
      this.pageNum = page
      this.pageSize = limit
    },
    resetQuery() {
      this.queryParams = {
        courseNo: undefined,
        courseName: undefined
      }
      this.pageNum = 1
      this.pageSize = 10
      this.fetchParams = {
        pageNum: 1,
        pageSize: 100,
        courseNo: undefined,
        courseName: undefined
      }
      this.getList()
    },
    handleExport() {
      this.download('edu/teacher/courses/export', {
        onlyCurrent: false,
        courseNo: this.queryParams.courseNo,
        courseName: this.queryParams.courseName
      }, 'history_courses_' + new Date().getTime() + '.xlsx')
    },
    viewStudents(row) {
      this.currentCourse = row
      this.activeTab = 'students'
      this.studentLoading = true
      getCourseStudents(row.offeringId, this.studentQuery.keyword).then(response => {
        this.studentList = (response.rows || []).map(this.normalizeStudentEnrollment)
        this.studentLoading = false
        this.open = true
      })
    },
    viewScores(row) {
      this.currentCourse = row
      this.activeTab = 'statistics'
      this.scoreLoading = true
      getCourseStudents(row.offeringId).then(response => {
        this.scoreList = (response.rows || []).map(this.normalizeStudentEnrollment)
        this.statistics = this.calculateStatistics(this.scoreList)
        this.scoreLoading = false
        this.open = true
      })
    },
    handleStudentQuery() {
      if (!this.currentCourse) return
      this.studentLoading = true
      getCourseStudents(this.currentCourse.offeringId, this.studentQuery.keyword).then(response => {
        this.studentList = (response.rows || []).map(this.normalizeStudentEnrollment)
        this.studentLoading = false
      })
    },
    resetStudentQuery() {
      this.studentQuery.keyword = undefined
      this.handleStudentQuery()
    },
    normalizeStudentEnrollment(row) {
      const student = row.student || {}
      const dept = student.dept || {}
      return {
        ...row,
        student: {
          ...student,
          dept: {
            ...dept,
            deptName: dept.deptName || student.deptName || row.deptName || row.studentDeptName || ''
          },
          phone: student.phone || student.phonenumber || row.phone || row.phonenumber || row.studentPhone || ''
        },
        studentDeptName: dept.deptName || student.deptName || row.deptName || row.studentDeptName || '',
        studentPhone: student.phone || student.phonenumber || row.phone || row.phonenumber || row.studentPhone || ''
      }
    },
    exportStudents() {
      if (!this.currentCourse) return
      this.download('edu/enrollment/export', {
        offeringId: this.currentCourse.offeringId,
        ...this.studentQuery
      }, 'students_' + new Date().getTime() + '.xlsx')
    },
    exportScores() {
      if (!this.currentCourse) return
      this.download('edu/enrollment/export', {
        offeringId: this.currentCourse.offeringId
      }, 'scores_' + new Date().getTime() + '.xlsx')
    },
    cancel() {
      this.open = false
      this.currentCourse = null
      this.studentList = []
      this.scoreList = []
      this.statistics = {
        totalStudents: 0,
        averageScore: 0,
        passRate: 0,
        excellentRate: 0
      }
    }
  }
}
</script>

<style scoped>
.empty-info {
  padding: 20px 0;
  text-align: center;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}
.grade-excellent {
  color: #67c23a;
  font-weight: bold;
}
.grade-pass {
  color: #409eff;
}
.grade-fail {
  color: #f56c6c;
}
</style>
