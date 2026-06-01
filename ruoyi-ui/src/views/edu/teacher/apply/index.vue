<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="mb8">
      <el-form-item label="申请学期">
        <el-select
          v-model="queryParams.xq"
          placeholder="请选择学期"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="semester in semesterOptions"
            :key="semester.value"
            :label="semester.label"
            :value="semester.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="课程名称">
        <el-input
          v-model="queryParams.km"
          placeholder="请输入课程名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="queryParams.stats"
          placeholder="请选择状态"
          clearable
          style="width: 150px"
        >
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新增申请</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
    </el-row>

    <el-card class="box-card" v-if="total > 0">
      <el-table v-loading="loading" :data="currentPageData" border>
        <el-table-column label="申请学期" align="center" prop="xq" width="180" />
        <el-table-column label="课程名称" align="center" prop="km" />
        <el-table-column label="学分" align="center" prop="xf" width="80" />
        <el-table-column label="开课院系" align="center" prop="yxm" width="150" />
        <el-table-column label="申请时间" align="center" prop="createTime" width="180" />
        <el-table-column label="状态" align="center" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.stats)">
              {{ getStatusText(scope.row.stats) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleView(scope.row)"
            >查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 10"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
    <div v-else class="empty-info">
      <el-empty description="暂无开课申请记录" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form :model="form" ref="form" :rules="rules" label-width="100px">
        <el-form-item label="学期" prop="xq">
          <el-select v-model="form.xq" placeholder="请选择学期" @change="checkScheduleConflict">
            <el-option
              v-for="semester in availableSemesters"
              :key="semester.value"
              :label="semester.label"
              :value="semester.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="课程类型">
          <el-radio-group v-model="form.courseType" @change="handleCourseTypeChange">
            <el-radio label="existing">选择已有课程</el-radio>
            <el-radio label="new">新建课程</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择课程" prop="courseId" v-if="form.courseType === 'existing'">
          <el-select
            v-model="form.courseId"
            placeholder="请选择课程"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="course in courseOptions"
              :key="course.value"
              :label="course.label"
              :value="course.value"
            />
          </el-select>
        </el-form-item>

        <template v-if="form.courseType === 'new'">
          <el-form-item label="课程名称" prop="km">
            <el-input v-model="form.km" placeholder="请输入课程名称" />
          </el-form-item>
          <el-form-item label="学分" prop="xf">
            <el-input v-model.number="form.xf" type="number" placeholder="请输入学分" />
          </el-form-item>
          <el-form-item label="学时" prop="xs">
            <el-input v-model.number="form.xs" type="number" placeholder="请输入学时" />
          </el-form-item>
          <el-form-item label="开课院系" prop="yxhId">
            <el-select v-model="form.yxhId" placeholder="请选择院系">
              <el-option
                v-for="dept in deptOptions"
                :key="dept.value"
                :label="dept.label"
                :value="dept.value"
              />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item label="期望星期（可选）">
          <el-select v-model="form.weekDay" placeholder="请选择星期（选填）" clearable @change="checkScheduleConflict">
            <el-option label="周一" value="周一" />
            <el-option label="周二" value="周二" />
            <el-option label="周三" value="周三" />
            <el-option label="周四" value="周四" />
            <el-option label="周五" value="周五" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望时间段（可选）">
          <el-select v-model="form.period" placeholder="请选择时间段（选填）" clearable @change="checkScheduleConflict">
            <el-option label="1-2节" value="1-2节" />
            <el-option label="3-4节" value="3-4节" />
            <el-option label="5-6节" value="5-6节" />
            <el-option label="7-8节" value="7-8节" />
            <el-option label="9-10节" value="9-10节" />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="scheduleConflictMessage"
          :title="scheduleConflictMessage"
          type="warning"
          show-icon
          :closable="false"
          class="schedule-conflict-alert"
        />
        <el-form-item label="希望容量" prop="expectedCapacity">
          <el-input-number v-model="form.expectedCapacity" :min="1" :max="999" controls-position="right" />
        </el-form-item>

        <el-form-item label="申请理由（可选）">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="请输入申请理由（选填）"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm">提交申请</el-button>
      </div>
    </el-dialog>

    <el-dialog title="申请详情" :visible.sync="viewOpen" width="600px" append-to-body>
      <el-form :model="viewForm" label-width="100px" disabled>
        <el-form-item label="申请学期">
          <span>{{ viewForm.xq }}</span>
        </el-form-item>
        <el-form-item label="课程名称">
          <span>{{ viewForm.km }}</span>
        </el-form-item>
        <el-form-item label="学分">
          <span>{{ viewForm.xf }}</span>
        </el-form-item>
        <el-form-item label="学时">
          <span>{{ viewForm.xs }}</span>
        </el-form-item>
        <el-form-item label="开课院系">
          <span>{{ viewForm.yxm }}</span>
        </el-form-item>
        <el-form-item label="申请时间">
          <span>{{ viewForm.createTime }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="getStatusType(viewForm.stats)">
            {{ getStatusText(viewForm.stats) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="拒绝理由" v-if="viewForm.stats === 'rejected'">
          <span>{{ viewForm.refuseReason }}</span>
        </el-form-item>
        <el-form-item label="申请理由">
          <span>{{ viewForm.remark || '-' }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getTeacherApplyList,
  submitTeacherApply,
  getTeacherApplyInfo,
  getTeacherSemesters,
  getTeacherDepartments,
  getTeacherCourseCatalog,
  getTeacherCourses
} from '@/api/edu/teacher'
import { getCurrentSemester } from '@/api/edu/semester'

export default {
  name: 'TeacherApply',
  data() {
    return {
      loading: true,
      open: false,
      viewOpen: false,
      title: '',
      total: 0,
      applyList: [],
      semesterOptions: [],
      availableSemesters: [],
      deptOptions: [],
      courseOptions: [],
      scheduleConflictMessage: '',
      lastConflictKey: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        xq: undefined,
        km: undefined,
        stats: undefined
      },
      form: {
        xq: '',
        courseType: 'existing',
        courseId: '',
        km: '',
        xf: '',
        xs: '',
        yxhId: '',
        expectedCapacity: null,
        weekDay: '',
        period: '',
        remark: ''
      },
      viewForm: {},
      rules: {
        xq: [{ required: true, message: '请选择学期', trigger: 'change' }],
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        km: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
        xf: [{ required: true, message: '请输入学分', trigger: 'blur' }],
        xs: [{ required: true, message: '请输入学时', trigger: 'blur' }],
        yxhId: [{ required: true, message: '请选择院系', trigger: 'change' }],
        expectedCapacity: [{ required: true, message: '请输入希望容量', trigger: 'blur' }]
      }
    }
  },
  computed: {
    currentPageData() {
      const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
      const end = start + this.queryParams.pageSize
      return this.applyList.slice(start, end)
    }
  },
  created() {
    this.getList()
    this.loadSemesters()
    this.loadDepartments()
    this.loadCourses()
  },
  methods: {
    getList() {
      this.loading = true
      getTeacherApplyList(this.queryParams).then(response => {
        this.applyList = response.rows || []
        this.total = response.total || this.applyList.length
        this.loading = false
      })
    },
    loadSemesters() {
      getTeacherSemesters({}).then(response => {
        const list = response.rows || []
        this.semesterOptions = list.map(item => ({
          value: item.semesterValue,
          label: item.semesterDesc
        }))
        getCurrentSemester().then(res => {
          const currentSemesterCode = res.data ? res.data.semesterValue : ''
          this.availableSemesters = list.filter(item => item.semesterValue >= currentSemesterCode).map(item => ({
            value: item.semesterValue,
            label: item.semesterDesc
          }))
        })
      })
    },
    loadDepartments() {
      getTeacherDepartments({}).then(response => {
        this.deptOptions = (response.rows || []).map(item => ({
          value: item.deptId,
          label: item.deptName
        }))
      })
    },
    loadCourses() {
      getTeacherCourseCatalog({}).then(response => {
        this.courseOptions = (response.rows || []).map(item => ({
          value: item.courseId,
          label: item.courseNo + ' - ' + item.courseName
        }))
      })
    },
    handleCourseTypeChange() {
      this.form.courseId = ''
      this.form.km = ''
      this.form.xf = ''
      this.form.xs = ''
      this.form.yxhId = ''
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        xq: undefined,
        km: undefined,
        stats: undefined
      }
      this.getList()
    },
    handleAdd() {
      this.title = '新增开课申请'
      this.form = {
        xq: '',
        courseType: 'existing',
        courseId: '',
        km: '',
        xf: '',
        xs: '',
        yxhId: '',
        expectedCapacity: null,
        weekDay: '',
        period: '',
        remark: ''
      }
      this.scheduleConflictMessage = ''
      this.lastConflictKey = ''
      this.open = true
    },
    handleView(row) {
      getTeacherApplyInfo(row.id).then(response => {
        this.viewForm = response.data || {}
        this.viewOpen = true
      })
    },
    handleExport() {
      this.download('edu/teacher/apply/export', {
        ...this.queryParams
      }, 'teacher_apply_' + new Date().getTime() + '.xlsx')
    },
    submitForm() {
      const form = this.$refs['form']
      form.validate(valid => {
        if (valid) {
          if (this.scheduleConflictMessage) {
            this.$modal.msgWarning(this.scheduleConflictMessage)
            return
          }
          this.doSubmit()
        }
      })
    },
    checkScheduleConflict() {
      this.scheduleConflictMessage = ''
      const { xq, weekDay, period } = this.form
      if (!xq || !weekDay || !period) {
        this.lastConflictKey = ''
        return
      }

      const conflictKey = [xq, weekDay, period].join('|')
      getTeacherCourses({ onlyCurrent: false }).then(response => {
        const rows = response.rows || []
        const conflict = rows.find(course => {
          return course.semester === xq && this.normalizeSchedule(course.schedule) === weekDay + period
        })
        if (!conflict) {
          if (this.lastConflictKey === conflictKey) {
            this.lastConflictKey = ''
          }
          return
        }

        this.scheduleConflictMessage = `该时间段已有课程“${conflict.courseName}”，请选择其他时间段`
        if (this.lastConflictKey !== conflictKey) {
          this.$modal.msgWarning(this.scheduleConflictMessage)
          this.lastConflictKey = conflictKey
        }
      })
    },
    normalizeSchedule(schedule) {
      if (!schedule) return ''
      const firstSchedule = schedule.split(/[,，;；]/)[0].trim()
      const weekMatch = firstSchedule.match(/^(周一|周二|周三|周四|周五|周六|周日)/)
      if (!weekMatch) return ''
      const periodMatch = firstSchedule.match(/(\d+)\s*-\s*(\d+)\s*节/)
      if (periodMatch) {
        return `${weekMatch[1]}${periodMatch[1]}-${periodMatch[2]}节`
      }
      const timeMatch = firstSchedule.match(/(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})/)
      if (timeMatch) {
        const startMinutes = Number(timeMatch[1]) * 60 + Number(timeMatch[2])
        const endMinutes = Number(timeMatch[3]) * 60 + Number(timeMatch[4])
        const period = this.convertTimeRangeToPeriod(startMinutes, endMinutes)
        return period ? weekMatch[1] + period : ''
      }
      return ''
    },
    convertTimeRangeToPeriod(startMinutes, endMinutes) {
      const ranges = [
        { value: '1-2节', start: 8 * 60, end: 9 * 60 + 40 },
        { value: '3-4节', start: 10 * 60, end: 11 * 60 + 40 },
        { value: '5-6节', start: 14 * 60, end: 15 * 60 + 40 },
        { value: '7-8节', start: 16 * 60, end: 17 * 60 + 40 },
        { value: '9-10节', start: 19 * 60, end: 20 * 60 + 40 }
      ]
      const matched = ranges.find(item => startMinutes === item.start && endMinutes === item.end)
      return matched ? matched.value : ''
    },
    doSubmit() {
      const schedule = this.form.weekDay && this.form.period ? this.formatCourseSchedule(this.form.weekDay, this.form.period) : ''
      const data = {
        xq: this.form.xq,
        courseType: this.form.courseType,
        courseId: this.form.courseType === 'existing' ? this.form.courseId : null,
        km: this.form.km,
        xf: this.form.xf,
        xs: this.form.xs,
        yxhId: this.form.yxhId,
        expectedCapacity: this.form.expectedCapacity,
        schedule: schedule,
        remark: this.form.remark
      }
      submitTeacherApply(data).then(() => {
        this.$modal.msgSuccess('申请提交成功')
        this.open = false
        this.getList()
      })
    },
    cancel() {
      this.open = false
      this.scheduleConflictMessage = ''
      this.lastConflictKey = ''
      this.form = {
        xq: '',
        courseType: 'existing',
        courseId: '',
        km: '',
        xf: '',
        xs: '',
        yxhId: '',
        expectedCapacity: null,
        weekDay: '',
        period: '',
        remark: ''
      }
    },
    formatCourseSchedule(weekDay, period) {
      const timeMap = {
        '1-2节': '8:00-9:40',
        '3-4节': '10:00-11:40',
        '5-6节': '14:00-15:40',
        '7-8节': '16:00-17:40',
        '9-10节': '19:00-20:40'
      }
      return weekDay + ' ' + (timeMap[period] || period)
    },
    getStatusType(status) {
      switch (status) {
        case 'pending':
          return 'warning'
        case 'approved':
          return 'success'
        case 'rejected':
          return 'danger'
        default:
          return ''
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'pending':
          return '待审核'
        case 'approved':
          return '已通过'
        case 'rejected':
          return '已拒绝'
        default:
          return status
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

.schedule-conflict-alert {
  margin: -8px 0 16px 100px;
  width: calc(100% - 100px);
}
</style>
