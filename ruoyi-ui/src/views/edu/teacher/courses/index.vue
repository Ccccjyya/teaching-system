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

    <div class="mb8">
      <div class="mb4">
        <h3 class="table-title">当前学期授课</h3>
        <p class="table-subtitle">共 {{ currentSemesterTotal }} 门课程</p>
      </div>
      <el-card class="box-card" v-if="currentSemesterTotal > 0">
        <el-table
          v-loading="loading"
          :data="currentSemesterCourses"
          border
        >
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
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-user"
                @click="viewStudents(scope.row)"
              >学生名单</el-button>
              <el-button
                size="mini"
                type="success"
                icon="el-icon-document"
                @click="enterScore(scope.row)"
              >成绩登分</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="currentSemesterTotal > 5"
          :total="currentSemesterTotal"
          :page.sync="currentPage.pageNum"
          :limit.sync="currentPage.pageSize"
          @pagination="handleCurrentPageChange"
        />
      </el-card>
      <div v-else class="empty-info">
        <el-empty description="暂无当前学期授课信息" />
      </div>
    </div>

    <div>
      <div class="mb4">
        <h3 class="table-title">历史学期授课</h3>
        <p class="table-subtitle">共 {{ historyTotal }} 门课程</p>
      </div>
      <el-card class="box-card" v-if="historyTotal > 0">
        <el-table
          v-loading="loading"
          :data="historyCourses"
          border
        >
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
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-user"
                @click="viewStudents(scope.row)"
              >学生名单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="historyTotal > 5"
          :total="historyTotal"
          :page.sync="historyPage.pageNum"
          :limit.sync="historyPage.pageSize"
          @pagination="handleHistoryPageChange"
        />
      </el-card>
      <div v-else class="empty-info">
        <el-empty description="暂无历史学期授课信息" />
      </div>
    </div>

    <el-dialog :title="(currentCourse && currentCourse.courseName) || '课程详情'" :visible.sync="open" width="900px" append-to-body>
      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
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
          <el-table
            v-loading="studentLoading"
            :data="studentList"
            border
          >
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
        <el-tab-pane label="成绩登分" name="scores">
          <el-form :model="scoreConfig" ref="scoreConfigForm" size="small" :inline="true" class="mb8">
            <el-form-item label="成绩权重设置">
              <el-input
                v-model.number="scoreConfig.usualPercent"
                type="number"
                :min="0"
                :max="100"
                style="width: 60px"
              />
              <span>% 平时成绩 + </span>
              <el-input
                v-model.number="scoreConfig.examPercent"
                type="number"
                :min="0"
                :max="100"
                style="width: 60px"
              />
              <span>% 考试成绩 = 总评成绩</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="mini" @click="updateScoreConfig">应用设置</el-button>
            </el-form-item>
          </el-form>
          <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button type="success" icon="el-icon-download" size="mini" @click="exportScoreTemplate">导出模板</el-button>
            </el-col>
          </el-row>
          <el-table
            v-loading="scoreLoading"
            :data="scoreList"
            border
          >
            <el-table-column label="学号" align="center" prop="student.studentNo" width="120" />
            <el-table-column label="姓名" align="center" prop="student.studentName" width="100" />
            <el-table-column label="平时成绩" align="center" width="120">
              <template slot-scope="scope">
                <el-input
                  v-model.number="scope.row.usualScore"
                  type="number"
                  :min="0"
                  :max="100"
                  style="width: 100px"
                />
              </template>
            </el-table-column>
            <el-table-column label="考试成绩" align="center" width="120">
              <template slot-scope="scope">
                <el-input
                  v-model.number="scope.row.examScore"
                  type="number"
                  :min="0"
                  :max="100"
                  style="width: 100px"
                />
              </template>
            </el-table-column>
            <el-table-column label="总评成绩" align="center" width="100">
              <template slot-scope="scope">
                <span>{{ calculateTotal(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="100">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="primary"
                  icon="el-icon-check"
                  @click="saveSingle(scope.row)"
                >保存</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div slot="footer" class="dialog-footer">
            <el-button type="warning" @click="resetAll">重置所有</el-button>
            <el-button type="primary" @click="saveAll">批量保存</el-button>
            <el-button @click="cancel">关闭</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { getTeacherCourses } from '@/api/edu/teacher'
import { getCourseStudents, saveScore, saveScores } from '@/api/edu/enrollment'
import { getCurrentSemester } from '@/api/edu/semester'
import { getGlobalSettings } from '@/api/edu/global'

export default {
  name: 'TeacherCourses',
  data() {
    return {
      loading: true,
      studentLoading: false,
      scoreLoading: false,
      courseList: [],
      studentList: [],
      scoreList: [],
      total: 0,
      open: false,
      activeTab: 'students',
      currentCourse: null,
      currentSemester: '',
      currentSemesterInfo: null,
      currentSemesterTotal: 0,
      historyTotal: 0,
      currentPage: {
        pageNum: 1,
        pageSize: 5
      },
      historyPage: {
        pageNum: 1,
        pageSize: 5
      },
      queryParams: {
        pageNum: 1,
        pageSize: 100,
        courseNo: undefined,
        courseName: undefined
      },
      studentQuery: {
        keyword: undefined
      },
      scoreConfig: {
        usualPercent: 30,
        examPercent: 70
      },
      isScoreEntryOpen: true
    }
  },
  computed: {
    allCurrentSemesterCourses() {
      return this.courseList.filter(course => {
        if (!course.semester || !this.currentSemester) return false
        return course.semester === this.currentSemester
      })
    },
    allHistoryCourses() {
      return this.courseList.filter(course => {
        if (!course.semester) return true
        return course.semester !== this.currentSemester
      })
    },
    currentSemesterCourses() {
      const list = this.allCurrentSemesterCourses
      this.currentSemesterTotal = list.length
      const start = (this.currentPage.pageNum - 1) * this.currentPage.pageSize
      const end = start + this.currentPage.pageSize
      return list.slice(start, end)
    },
    historyCourses() {
      const list = this.allHistoryCourses
      this.historyTotal = list.length
      const start = (this.historyPage.pageNum - 1) * this.historyPage.pageSize
      const end = start + this.historyPage.pageSize
      return list.slice(start, end)
    }
  },
  created() {
    this.loadScoreEntrySwitch()
    this.loadCurrentSemester()
    this.getList()
  },
  methods: {
    loadScoreEntrySwitch() {
      getGlobalSettings().then(response => {
        const data = response.data || {}
        this.isScoreEntryOpen = !!data.is_score_entry_open
      })
    },
    loadCurrentSemester() {
      getCurrentSemester().then(response => {
        if (response.data) {
          this.currentSemesterInfo = response.data
          this.currentSemester = response.data.semesterValue
        }
      })
    },
    formatSemester(semesterCode) {
      if (!semesterCode) return '-'
      const parts = semesterCode.split('-')
      if (parts.length >= 3) {
        const term = parts[2]
        const termName = term === '1' ? '秋季学期' : term === '2' ? '春季学期' : term
        return `${parts[0]}-${parts[1]}${termName}`
      }
      return semesterCode
    },
    getList() {
      this.loading = true
      getTeacherCourses(this.queryParams).then(response => {
        this.courseList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.currentPage.pageNum = 1
      this.historyPage.pageNum = 1
      this.getList()
    },
    handleCurrentPageChange() {
    },
    handleHistoryPageChange() {
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 100,
        courseNo: undefined,
        courseName: undefined
      }
      this.currentPage.pageNum = 1
      this.historyPage.pageNum = 1
      this.getList()
    },
    handleExport() {
      this.download('edu/teacher/courses/export', {
        ...this.queryParams
      }, 'courses_' + new Date().getTime() + '.xlsx')
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
    enterScore(row) {
      if (!this.isScoreEntryOpen) {
        this.$modal.msgWarning('目前未开启登分')
        return
      }
      this.currentCourse = row
      this.activeTab = 'scores'
      this.scoreLoading = true
      getCourseStudents(row.offeringId).then(response => {
        this.scoreList = (response.rows || []).map(this.normalizeStudentEnrollment).map(s => ({
          ...s,
          usualScore: s.usualScore || '',
          examScore: s.examScore || ''
        }))
        this.scoreLoading = false
        this.open = true
      })
    },
    loadStudents(offeringId) {
      this.studentLoading = true
      getCourseStudents(offeringId, this.studentQuery.keyword).then(response => {
        this.studentList = (response.rows || []).map(this.normalizeStudentEnrollment)
        this.studentLoading = false
      })
    },
    loadScores(offeringId) {
      this.scoreLoading = true
      getCourseStudents(offeringId).then(response => {
        this.scoreList = (response.rows || []).map(this.normalizeStudentEnrollment).map(s => ({
          ...s,
          usualScore: s.usualScore || '',
          examScore: s.examScore || ''
        }))
        this.scoreLoading = false
      })
    },
    handleTabClick(tab) {
      if (!this.currentCourse) return
      if (tab.name === 'students') {
        this.loadStudents(this.currentCourse.offeringId)
      }
      if (tab.name === 'scores') {
        this.loadScores(this.currentCourse.offeringId)
      }
    },
    handleStudentQuery() {
      if (this.currentCourse) {
        this.loadStudents(this.currentCourse.offeringId)
      }
    },
    resetStudentQuery() {
      this.studentQuery.keyword = undefined
      this.handleStudentQuery()
    },
    updateScoreConfig() {
      if (this.scoreConfig.usualPercent + this.scoreConfig.examPercent !== 100) {
        this.$modal.msgError('平时成绩和考试成绩百分比之和必须等于100%')
        return
      }
      this.$modal.msgSuccess('设置已应用')
    },
    calculateTotal(row) {
      const usual = parseFloat(row.usualScore) || 0
      const exam = parseFloat(row.examScore) || 0
      const usualPercent = this.scoreConfig.usualPercent / 100
      const examPercent = this.scoreConfig.examPercent / 100
      return Math.round(usual * usualPercent + exam * examPercent)
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
    saveSingle(row) {
      if (!this.isScoreEntryOpen) {
        this.$modal.msgWarning('目前未开启登分')
        return
      }
      const totalScore = this.calculateTotal(row)
      saveScore({
        enrollmentId: row.enrollmentId,
        usualScore: row.usualScore,
        examScore: row.examScore,
        totalScore: totalScore
      }).then(() => {
        this.$modal.msgSuccess('保存成功')
        row.totalScore = totalScore
      })
    },
    saveAll() {
      if (!this.isScoreEntryOpen) {
        this.$modal.msgWarning('目前未开启登分')
        return
      }
      const scores = this.scoreList.map(s => ({
        enrollmentId: s.enrollmentId,
        usualScore: s.usualScore,
        examScore: s.examScore,
        totalScore: this.calculateTotal(s)
      }))
      saveScores(scores).then(() => {
        this.$modal.msgSuccess('批量保存成功')
        this.scoreList.forEach(s => {
          s.totalScore = this.calculateTotal(s)
        })
      })
    },
    resetAll() {
      this.scoreList.forEach(s => {
        s.usualScore = ''
        s.examScore = ''
      })
    },
    exportStudents() {
      if (this.currentCourse) {
        this.download('edu/enrollment/export', {
          offeringId: this.currentCourse.offeringId,
          ...this.studentQuery
        }, 'students_' + new Date().getTime() + '.xlsx')
      }
    },
    exportScoreTemplate() {
      this.$modal.msgInfo('导出模板功能开发中')
    },
    cancel() {
      this.open = false
      this.currentCourse = null
      this.studentList = []
      this.scoreList = []
    }
  }
}
</script>

<style scoped>
.table-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.table-subtitle {
  font-size: 14px;
  color: #909399;
}

.mb4 {
  margin-bottom: 16px;
}

.empty-info {
  padding: 20px 0;
  text-align: center;
}
</style>
