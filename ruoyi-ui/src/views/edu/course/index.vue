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
      <el-form-item label="院系">
        <el-select v-model="queryParams.deptId" placeholder="请选择院系" clearable>
          <el-option v-for="dept in deptOptions" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId"></el-option>
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
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['edu:course:add']"
        >新增课程</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['edu:course:export']"
        >导出</el-button>
      </el-col>
    </el-row>

    <el-table
      v-loading="loading"
      :data="courseList"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="课程号" align="center" prop="courseNo" />
      <el-table-column label="课程名" align="center" prop="courseName" />
      <el-table-column label="学分" align="center" prop="credit" />
      <el-table-column label="学时" align="center" prop="hours" />
      <el-table-column label="院系" align="center" prop="deptName" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['edu:course:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['edu:course:remove']"
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

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程号" prop="courseNo">
          <el-input v-model="form.courseNo" placeholder="请输入课程号" />
        </el-form-item>
        <el-form-item label="课程名" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input v-model.number="form.credit" type="number" placeholder="请输入学分" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input v-model.number="form.hours" type="number" placeholder="请输入学时" />
        </el-form-item>
        <el-form-item label="院系" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择院系">
            <el-option v-for="dept in deptOptions" :key="dept.deptId" :label="dept.deptName" :value="dept.deptId"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { listCourse, getCourse, addCourse, updateCourse, delCourse } from '@/api/edu/course'
import { listDepartment } from '@/api/edu/department'

export default {
  name: 'Course',
  data() {
    return {
      loading: true,
      courseList: [],
      total: 0,
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseNo: undefined,
        courseName: undefined,
        deptId: undefined
      },
      form: {},
      deptOptions: [],
      ids: [],
      rules: {
        courseNo: [
          { required: true, message: '课程号不能为空', trigger: 'blur' },
          { max: 20, message: '课程号长度不能超过20个字符', trigger: 'blur' }
        ],
        courseName: [
          { required: true, message: '课程名不能为空', trigger: 'blur' },
          { max: 50, message: '课程名长度不能超过50个字符', trigger: 'blur' }
        ],
        credit: [
          { required: true, message: '学分不能为空', trigger: 'blur' }
        ],
        hours: [
          { required: true, message: '学时不能为空', trigger: 'blur' }
        ],
        deptId: [
          { required: true, message: '院系不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getDeptOptions()
  },
  methods: {
    getList() {
      this.loading = true
      listCourse(this.queryParams).then(response => {
        this.courseList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getDeptOptions() {
      listDepartment({}).then(response => {
        this.deptOptions = response.rows
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
        courseNo: undefined,
        courseName: undefined,
        deptId: undefined
      }
      this.getList()
    },
    handleAdd() {
      this.form = {}
      this.title = '新增课程'
      this.open = true
    },
    handleEdit(row) {
      this.form = Object.assign({}, row)
      this.title = '修改课程'
      this.open = true
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除课程编号为"' + row.courseNo + '"的数据项？').then(function() {
        return delCourse(row.courseId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.courseId)
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.courseId != null) {
            updateCourse(this.form).then(() => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addCourse(this.form).then(() => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    cancel() {
      this.open = false
      this.form = {}
    },
    handleExport() {
      this.$modal.confirm('是否确认导出所有课程数据？').then(() => {
        return this.download('edu/course/export', {
          ...this.queryParams
        }, 'course.xlsx')
      }).catch(() => {})
    }
  }
}
</script>
