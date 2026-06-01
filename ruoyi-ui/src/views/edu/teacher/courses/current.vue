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
            <el-button size="mini" type="success" icon="el-icon-document" @click="enterScore(scope.row)">成绩登分</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > queryParams.pageSize"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="handlePageChange"
      />
    </el-card>
    <div v-else class="empty-info">
      <el-empty description="暂无当前学期授课信息" />
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
        <el-tab-pane label="成绩登分" name="scores">
          <el-form :model="scoreConfig" ref="scoreConfigForm" size="small" :inline="true" class="mb8">
            <el-form-item label="成绩权重设置">
              <el-input v-model.number="scoreConfig.usualPercent" type="number" :min="0" :max="100" style="width: 60px" />
              <span>% 平时成绩 + </span>
              <el-input v-model.number="scoreConfig.examPercent" type="number" :min="0" :max="100" style="width: 60px" />
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
          <el-table v-loading="scoreLoading" :data="scoreList" border>
            <el-table-column label="学号" align="center" prop="student.studentNo" width="120" />
            <el-table-column label="姓名" align="center" prop="student.studentName" width="100" />
            <el-table-column label="平时成绩" align="center" width="120">
              <template slot-scope="scope">
                <el-input v-model.number="scope.row.usualScore" type="number" :min="0" :max="100" style="width: 100px" />
              </template>
            </el-table-column>
            <el-table-column label="考试成绩" align="center" width="120">
              <template slot-scope="scope">
                <el-input v-model.number="scope.row.examScore" type="number" :min="0" :max="100" style="width: 100px" />
              </template>
            </el-table-column>
            <el-table-column label="总评成绩" align="center" width="100">
              <template slot-scope="scope">
                <span>{{ calculateTotal(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="100">
              <template slot-scope="scope">
                <el-button size="mini" type="primary" icon="el-icon-check" @click="saveSingle(scope.row)">保存</el-button>
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
  name: 'CurrentCourses',
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
        pageNum: 1,
        pageSize: 5,
        courseNo: undefined,
        courseName: undefined
      },
      fetchParams: {
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
    filteredCourses() {
      return (this.allCourses || []).filter(course => course.semester && this.currentSemester && course.semester === this.currentSemester)
    },
    currentPageData() {
      const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
      const end = start + this.queryParams.pageSize
      return this.filteredCourses.slice(start, end)
    }
  },
  created() {
    this.loadScoreEntrySwitch()
    this.loadCurrentSemester()
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
        this.currentSemester = response.data ? response.data.semesterValue : ''
        this.getList()
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
      this.fetchParams.courseNo = this.queryParams.courseNo
      this.fetchParams.courseName = this.queryParams.courseName
      getTeacherCourses(this.fetchParams).then(response => {
        this.allCourses = response.rows || []
        this.total = this.filteredCourses.length
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handlePageChange() {},
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 5,
        courseNo: undefined,
        courseName: undefined
      }
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
        onlyCurrent: true,
        courseNo: this.queryParams.courseNo,
        courseName: this.queryParams.courseName
      }, 'current_courses_' + new Date().getTime() + '.xlsx')
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
        totalScore
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
      if (!this.currentCourse) return
      this.download('edu/enrollment/export', {
        offeringId: this.currentCourse.offeringId,
        ...this.studentQuery
      }, 'students_' + new Date().getTime() + '.xlsx')
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
.empty-info {
  padding: 20px 0;
  text-align: center;
}
</style>
