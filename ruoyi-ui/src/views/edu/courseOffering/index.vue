<template>
  <div class="app-container wrap-table">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="mb8">
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable>
          <el-option
            v-for="sem in semesterList"
            :key="sem.semesterValue"
            :label="sem.semesterName"
            :value="sem.semesterValue"
          ></el-option>
        </el-select>
      </el-form-item>
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
        <el-button
          type="success"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['edu:offering:export']"
        >导出</el-button>
      </el-col>
    </el-row>

    <el-table
      v-loading="loading"
      :data="offeringList"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="学期" align="center" min-width="170">
        <template slot-scope="scope">
          {{ formatSemester(scope.row.semester) }}
        </template>
      </el-table-column>
      <el-table-column label="课程号" align="center" prop="courseNo" min-width="110" />
      <el-table-column label="课程名" align="center" min-width="130">
        <template slot-scope="scope">
          {{ scope.row.course ? scope.row.course.courseName : '' }}
        </template>
      </el-table-column>
      <el-table-column label="教师" align="center" min-width="100">
        <template slot-scope="scope">
          {{ scope.row.teacher ? scope.row.teacher.teacherName : '' }}
        </template>
      </el-table-column>
      <el-table-column label="上课时间" align="center" prop="schedule" min-width="120" />
      <el-table-column label="上课地点" align="center" prop="location" min-width="120" />
      <el-table-column label="选课人数" align="center" prop="selectedCount" min-width="90" />
      <el-table-column label="容量" align="center" prop="maxCapacity" min-width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" min-width="90">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['edu:offering:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['edu:offering:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="修改开课信息" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程号" prop="courseNo">
          <el-input v-model="form.courseNo" placeholder="请输入课程号" />
        </el-form-item>
        <el-form-item label="课程名">
          <el-input :value="form.course ? form.course.courseName : ''" disabled />
        </el-form-item>
        <el-form-item label="教师">
          <el-input :value="form.teacher ? form.teacher.teacherName : ''" disabled />
        </el-form-item>
        <el-form-item label="星期" prop="weekDay">
          <el-select v-model="form.weekDay" placeholder="请选择星期">
            <el-option label="周一" value="周一" />
            <el-option label="周二" value="周二" />
            <el-option label="周三" value="周三" />
            <el-option label="周四" value="周四" />
            <el-option label="周五" value="周五" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="period">
          <el-select v-model="form.period" placeholder="请选择时间段">
            <el-option label="1-2节" value="1-2节" />
            <el-option label="3-4节" value="3-4节" />
            <el-option label="5-6节" value="5-6节" />
            <el-option label="7-8节" value="7-8节" />
            <el-option label="9-10节" value="9-10节" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入上课地点" />
        </el-form-item>
        <el-form-item label="容量" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="1" :max="999" controls-position="right" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { adminListCourseOffering, getCourseOffering, updateCourseOffering, delCourseOffering } from '@/api/edu/courseOffering'
import { listSemester } from '@/api/edu/global'

export default {
  name: 'CourseOffering',
  data() {
    return {
      loading: true,
      offeringList: [],
      total: 0,
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        semester: undefined,
        courseNo: undefined,
        courseName: undefined
      },
      form: {
        course: {},
        teacher: {}
      },
      semesterList: [],
      ids: [],
      rules: {
        courseNo: [
          { max: 20, message: '课程号长度不能超过20个字符', trigger: 'blur' }
        ],
        weekDay: [
          { required: true, message: '请选择星期', trigger: 'change' }
        ],
        period: [
          { required: true, message: '请选择时间段', trigger: 'change' }
        ],
        location: [
          { required: true, message: '上课地点不能为空', trigger: 'blur' },
          { max: 50, message: '上课地点长度不能超过50个字符', trigger: 'blur' }
        ],
        maxCapacity: [
          { required: true, message: '容量不能为空', trigger: 'blur' },
          { type: 'number', min: 1, message: '容量必须大于0', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getSemesterList()
  },
  methods: {
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
    getList() {
      this.loading = true
      adminListCourseOffering(this.queryParams).then(response => {
        this.offeringList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getSemesterList() {
      listSemester({}).then(response => {
        this.semesterList = response.rows
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        semester: undefined,
        courseNo: undefined,
        courseName: undefined
      }
      this.getList()
    },
    handleEdit(row) {
      getCourseOffering(row.offeringId).then(response => {
        this.form = Object.assign({}, { course: {}, teacher: {} }, response.data)
        if (this.form.schedule) {
          const match = this.form.schedule.match(/^(周一|周二|周三|周四|周五)(.+)$/)
          if (match) {
            this.form.weekDay = match[1]
            this.form.period = match[2]
          }
        }
        this.open = true
      })
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.offeringId)
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.form.schedule = this.form.weekDay + this.form.period
          updateCourseOffering(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除课程号为“' + row.courseNo + '”的开课记录？').then(() => {
        return delCourseOffering(row.offeringId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      }).catch(() => {})
    },
    cancel() {
      this.open = false
      this.form = { course: {}, teacher: {} }
    },
    handleExport() {
      const _this = this
      this.$modal.confirm('是否确认导出所有开课数据？').then(function() {
        return _this.download('edu/offering/export', {
          ..._this.queryParams
        }, 'offering.xlsx')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.wrap-table ::v-deep .el-table__cell .cell {
  white-space: normal;
  word-break: break-word;
}
</style>
