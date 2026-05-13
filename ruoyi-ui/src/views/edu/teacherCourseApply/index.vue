<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="mb8">
      <el-form-item label="学期">
        <el-select v-model="queryParams.xq" placeholder="请选择学期" clearable>
          <el-option 
            v-for="sem in getSemesterOptions()" 
            :key="sem.value" 
            :label="sem.label" 
            :value="sem.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="课程名">
        <el-input
          v-model="queryParams.km"
          placeholder="请输入课程名"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="院系">
        <el-select v-model="queryParams.yxhId" placeholder="请选择院系" clearable>
          <el-option v-for="dept in deptOptions" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.stats" placeholder="请选择状态" clearable>
          <el-option label="待审核" value="pending"></el-option>
          <el-option label="已通过" value="approved"></el-option>
          <el-option label="已拒绝" value="rejected"></el-option>
        </el-select>
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
          v-hasPermi="['edu:apply:export']"
        >导出</el-button>
      </el-col>
    </el-row>

    <el-table
      v-loading="loading"
      :data="applyList"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学期" align="center" prop="xq">
        <template slot-scope="scope">
          <span>{{ scope.row.xq === '1' ? '秋季学期' : '春季学期' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="课程名" align="center" prop="km" />
      <el-table-column label="院系" align="center" prop="yxm" />
      <el-table-column label="学分" align="center" prop="xf" />
      <el-table-column label="教师工号" align="center" prop="gh" />
      <el-table-column label="教师姓名" align="center" prop="teacherName" />
      <el-table-column label="希望开课时间" align="center" prop="schedule" min-width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.schedule || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="stats">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.stats)">{{ getStatusText(scope.row.stats) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="拒绝原因" align="center" prop="refuseReason" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleView(scope.row)"
            v-hasPermi="['edu:apply:query']"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleCommit(scope.row)"
            v-if="scope.row.stats === 'pending'"
            v-hasPermi="['edu:apply:commit']"
          >审核通过</el-button>

          <el-button
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleRefuse(scope.row)"
            v-if="scope.row.stats === 'pending'"
            v-hasPermi="['edu:apply:refuse']"
          >审核拒绝</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['edu:apply:remove']"
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

    <el-dialog title="申请详情" :visible.sync="viewOpen" width="500px" append-to-body>
      <el-form ref="viewForm" :model="viewForm" label-width="80px" disabled>
        <el-form-item label="学期">
          <span>{{ viewForm.xq === '1' ? '秋季学期' : '春季学期' }}</span>
        </el-form-item>
        <el-form-item label="课程名">
          <span>{{ viewForm.km }}</span>
        </el-form-item>
        <el-form-item label="院系">
          <span>{{ viewForm.yxm }}</span>
        </el-form-item>
        <el-form-item label="学分">
          <span>{{ viewForm.xf }}</span>
        </el-form-item>
        <el-form-item label="教师工号">
          <span>{{ viewForm.gh }}</span>
        </el-form-item>
        <el-form-item label="教师姓名">
          <span>{{ viewForm.teacherName }}</span>
        </el-form-item>
        <el-form-item label="希望开课时间">
          <span>{{ viewForm.schedule || '-' }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="getStatusType(viewForm.stats)">{{ getStatusText(viewForm.stats) }}</el-tag>
        </el-form-item>
        <el-form-item label="拒绝原因">
          <span>{{ viewForm.refuseReason || '-' }}</span>
        </el-form-item>
        <el-form-item label="创建时间">
          <span>{{ viewForm.createTime }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <span>{{ viewForm.remark || '-' }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="viewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="审核通过" :visible.sync="commitOpen" width="450px" append-to-body>
      <el-form ref="commitForm" :model="commitForm" :rules="commitRules" label-width="80px">
        <el-form-item label="课程号" prop="courseNo">
          <el-input v-model="commitForm.courseNo" placeholder="自动生成，可自定义" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input v-model.number="commitForm.hours" type="number" placeholder="默认按学分计算" />
        </el-form-item>
        <el-form-item label="星期" prop="weekDay">
          <el-select v-model="commitForm.weekDay" placeholder="请选择星期">
            <el-option label="周一" value="周一" />
            <el-option label="周二" value="周二" />
            <el-option label="周三" value="周三" />
            <el-option label="周四" value="周四" />
            <el-option label="周五" value="周五" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="period">
          <el-select v-model="commitForm.period" placeholder="请选择时间段">
            <el-option label="1-2节" value="1-2节" />
            <el-option label="3-4节" value="3-4节" />
            <el-option label="5-6节" value="5-6节" />
            <el-option label="7-8节" value="7-8节" />
            <el-option label="9-10节" value="9-10节" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="commitForm.location" placeholder="如：教学楼A-301" />
        </el-form-item>
        <el-form-item label="容量" prop="maxCapacity">
          <el-input-number v-model="commitForm.maxCapacity" :min="1" :max="999" controls-position="right" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCommit">确 认 通过</el-button>
        <el-button @click="commitOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="审核拒绝" :visible.sync="refuseOpen" width="450px" append-to-body>
      <el-form ref="refuseForm" :model="refuseForm" :rules="refuseRules" label-width="80px">
        <el-form-item label="拒绝原因" prop="refuseReason">
          <el-input v-model="refuseForm.refuseReason" type="textarea" placeholder="请输入拒绝原因" :rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="danger" @click="submitRefuse">确 认 拒绝</el-button>
        <el-button @click="refuseOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listApply, getApply, delApply, applyCommit, applyRefuse } from '@/api/edu/teacherCourseApply'
import { listDepartment } from '@/api/edu/department'
import { listSemester } from '@/api/edu/global'

export default {
  name: 'TeacherCourseApply',
  data() {
    return {
      loading: true,
      applyList: [],
      total: 0,
      viewOpen: false,
      commitOpen: false,
      refuseOpen: false,
      semesterList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        xq: undefined,
        km: undefined,
        yxhId: undefined,
        stats: undefined
      },
      viewForm: {},
      commitForm: {
        id: '',
        courseNo: '',
        hours: '',
        weekDay: '',
        period: '',
        location: '',
        maxCapacity: null
      },
      refuseForm: {
        id: '',
        refuseReason: ''
      },
      deptOptions: [],
      ids: [],
      commitRules: {
        courseNo: [
          { max: 20, message: '课程号长度不能超过20个字符', trigger: 'blur' }
        ],
        hours: [
          { min: 0, message: '学时必须大于等于0', trigger: 'blur' }
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
          { required: true, message: '请填写容量', trigger: 'blur' },
          { type: 'number', min: 1, message: '容量必须大于0', trigger: 'blur' }
        ]
      },
      refuseRules: {
        refuseReason: [
          { required: true, message: '拒绝原因不能为空', trigger: 'blur' },
          { max: 500, message: '拒绝原因长度不能超过500个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getDeptOptions()
    this.getSemesterList()
  },
  methods: {
    getSemesterList() {
      listSemester({}).then(response => {
        this.semesterList = response.rows
      })
    },
    getList() {
      this.loading = true
      listApply(this.queryParams).then(response => {
        this.applyList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getDeptOptions() {
      listDepartment({}).then(response => {
        this.deptOptions = response.rows
      })
    },
    getStatusType(status) {
      switch (status) {
        case 'pending': return 'warning'
        case 'approved': return 'success'
        case 'rejected': return 'danger'
        default: return 'info'
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'pending': return '待审核'
        case 'approved': return '已通过'
        case 'rejected': return '已拒绝'
        default: return status
      }
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
        yxhId: undefined,
        stats: undefined
      }
      this.getList()
    },
    handleView(row) {
      getApply(row.id).then(response => {
        this.viewForm = response.data
        this.viewOpen = true
      })
    },
    handleCommit(row) {
      this.commitForm = {
        id: row.id,
        courseNo: '',
        hours: '',
        weekDay: '',
        period: '',
        location: '',
        maxCapacity: row.expectedCapacity || null
      }
      this.commitOpen = true
    },
    handleRefuse(row) {
      this.refuseForm = {
        id: row.id,
        refuseReason: ''
      }
      this.refuseOpen = true
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除该申请？').then(function() {
        return delApply(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
    },
    submitCommit() {
      this.$refs['commitForm'].validate(valid => {
        if (valid) {
          const params = {
            id: this.commitForm.id,
            courseNo: this.commitForm.courseNo,
            hours: this.commitForm.hours,
            schedule: this.commitForm.weekDay + this.commitForm.period,
            location: this.commitForm.location,
            maxCapacity: this.commitForm.maxCapacity
          }
          applyCommit(params).then(() => {
            this.$modal.msgSuccess('审核通过成功')
            this.commitOpen = false
            this.getList()
          })
        }
      })
    },
    submitRefuse() {
      this.$refs['refuseForm'].validate(valid => {
        if (valid) {
          applyRefuse(this.refuseForm).then(() => {
            this.$modal.msgSuccess('拒绝成功')
            this.refuseOpen = false
            this.getList()
          })
        }
      })
    },
    handleExport() {
      this.$modal.confirm('是否确认导出所有申请数据？').then(() => {
        return this.download('edu/apply/export', {
          ...this.queryParams
        }, 'apply.xlsx')
      }).catch(() => {})
    },
    getSemesterOptions() {
      return this.semesterList.map(s => {
        const parts = s.semesterValue.split('-')
        const sem = parts[2]
        return {
          label: sem === '1' ? '秋季学期' : sem === '2' ? '春季学期' : sem,
          value: sem
        }
      })
    }
  }
}
</script>
