<template>
  <div class="app-container">
    <!-- 选课未开放提示 -->
    <div v-if="!isSelectOpen" class="select-closed-container">
      <div class="select-closed-content">
        <i class="el-icon-info" style="font-size: 80px; color: #e6a23c; margin-bottom: 20px;"></i>
        <h2>当前未开放选课</h2>
        <p>请等待选课开放后再进行操作</p>
      </div>
    </div>

    <!-- 选课开放时的内容 -->
    <div v-else>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
        <el-form-item label="课程名称" prop="courseName">
          <el-input
            v-model="queryParams.courseName"
            placeholder="请输入课程名称"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="课号" prop="courseNo">
          <el-input
            v-model="queryParams.courseNo"
            placeholder="请输入课号"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="星期几" prop="weekday">
          <el-select v-model="queryParams.weekday" placeholder="请选择星期几" clearable style="width: 130px">
            <el-option v-for="item in weekdayOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="timePeriod">
          <el-select v-model="queryParams.timePeriod" placeholder="请选择时间段" clearable style="width: 160px">
            <el-option v-for="item in timePeriodOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否已满" prop="fullStatus">
          <el-select v-model="queryParams.fullStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option label="已满" value="1" />
            <el-option label="未满" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作栏 -->
      <el-row :gutter="10" class="mb8">
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <!-- 课程列表 -->
      <el-table v-loading="loading" :data="courseList" :fit="true" border>
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-form label-position="left" inline class="table-expand">
              <el-form-item label="课程学分：">
                <span>{{ props.row.course.credit }}</span>
              </el-form-item>
              <el-form-item label="课程学时：">
                <span>{{ props.row.course.hours }}</span>
              </el-form-item>
              <el-form-item label="上课地点：">
                <span>{{ props.row.location || '-' }}</span>
              </el-form-item>
              <el-form-item label="授课教师：">
                <span>{{ props.row.teacher && props.row.teacher.teacherName ? props.row.teacher.teacherName : '-' }}</span>
              </el-form-item>
            </el-form>
          </template>
        </el-table-column>
        <el-table-column label="课号" align="center" prop="courseNo" width="90" />
        <el-table-column label="课程名称" align="center" prop="course.courseName" min-width="200" />
        <el-table-column label="学期" align="center" width="140">
          <template slot-scope="scope">
            <div>{{ getAcademicYear(scope.row.semester) }}</div>
            <el-tag size="mini" :type="getSemesterType(scope.row.semester)">
              {{ getSemesterOnly(scope.row.semester) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上课时间" align="center" prop="schedule" min-width="180" />
        <el-table-column label="选课情况" align="center" width="150">
          <template slot-scope="scope">
            <div>{{ scope.row.selectedCount }} / {{ scope.row.maxCapacity }}</div>
            <el-progress
              :percentage="parseFloat((scope.row.selectedCount / scope.row.maxCapacity * 100).toFixed(2))"
              :color="getProgressColor(scope.row.selectedCount, scope.row.maxCapacity)"
              :stroke-width="6"
              style="margin-top: 3px"
            ></el-progress>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <el-button
              v-if="isSelected(scope.row.offeringId)"
              size="mini"
              type="success"
              icon="el-icon-check"
              disabled
            >已选</el-button>
            <el-button
              v-else
              size="mini"
              type="primary"
              icon="el-icon-plus"
              :disabled="scope.row.selectedCount >= scope.row.maxCapacity"
              @click="handleSelect(scope.row)"
            >选课</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script>
import { listCourseOffering } from "@/api/edu/courseOffering";
import { studentEnroll, myEnrollments } from "@/api/edu/enrollment";
import { listSemester, getCurrentSemester, getGlobalSettings } from "@/api/edu/global";

export default {
  name: "CourseSelection",
  data() {
    return {
      loading: true,
      showSearch: true,
      courseList: [],
      selectedOfferings: [],
      semesterOptions: [],
      currentSemester: null,
      total: 0,
      isSelectOpen: true,
      weekdayOptions: [
        { label: '周一', value: '周一' },
        { label: '周二', value: '周二' },
        { label: '周三', value: '周三' },
        { label: '周四', value: '周四' },
        { label: '周五', value: '周五' },
        { label: '周六', value: '周六' },
        { label: '周日', value: '周日' }
      ],
      timePeriodOptions: [
        { label: '1-2节', value: '1-2节' },
        { label: '3-4节', value: '3-4节' },
        { label: '5-6节', value: '5-6节' },
        { label: '7-8节', value: '7-8节' },
        { label: '9-10节', value: '9-10节' }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseName: undefined,
        courseNo: undefined,
        weekday: undefined,
        timePeriod: undefined,
        fullStatus: undefined
      }
    };
  },
  created() {
    this.checkSelectOpen();
    this.getSemesterOptions();
    this.getCurrentSemester();
  },
  methods: {
    checkSelectOpen() {
      getGlobalSettings().then(response => {
        this.isSelectOpen = response.data.is_select_open;
        if (this.isSelectOpen) {
          this.getList();
        } else {
          this.loading = false;
        }
      }).catch(() => {
        this.isSelectOpen = true;
        this.getList();
        this.loading = false;
      });
    },
    getList() {
      this.loading = true;
      Promise.all([
        listCourseOffering(this.queryParams),
        myEnrollments()
      ]).then(([courseResponse, enrollResponse]) => {
        this.courseList = courseResponse.rows;
        this.total = courseResponse.total;
        const enrollData = enrollResponse.data || [];
        this.selectedOfferings = enrollData.map(e => e.offeringId).filter(id => id !== undefined);
        this.loading = false;
      });
    },
    getSemesterOptions() {
      listSemester({}).then(response => {
        this.semesterOptions = response.rows;
      });
    },
    getCurrentSemester() {
      getCurrentSemester().then(response => {
        this.currentSemester = response.data;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelect(row) {
      this.$modal.confirm('确认选择课程"' + row.course.courseName + '"吗？').then(() => {
        let academicYear = "2024-2025";
        let semester = "1";
        if (this.currentSemester) {
          const parts = this.currentSemester.semesterValue.split("-");
          if (parts.length >= 3) {
            academicYear = parts[0] + "-" + parts[1];
            semester = parts[2];
          }
        }
        return studentEnroll({
          offeringId: row.offeringId,
          academicYear: academicYear,
          semester: semester
        });
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("选课成功！");
      }).catch(() => {});
    },
    getProgressColor(selected, max) {
      const percentage = (selected / max) * 100;
      if (percentage < 50) return "#67c23a";
      if (percentage < 80) return "#e6a23c";
      return "#f56c6c";
      },
      getAcademicYear(semester) {
        const sem = this.semesterOptions.find(s => s.semesterValue === semester);
        if (sem) {
          const parts = sem.semesterValue.split("-");
          if (parts.length >= 3) {
            return parts[0] + "-" + parts[1];
          }
        }
        if (!semester) return "-";
        const parts = semester.split("-");
        if (parts.length >= 3) {
          return parts[0] + "-" + parts[1];
        }
        return "-";
      },
      getSemesterOnly(semester) {
        const sem = this.semesterOptions.find(s => s.semesterValue === semester);
        if (sem) {
          const parts = sem.semesterValue.split("-");
          if (parts.length >= 3) {
            const term = parts[2];
            return term === "1" ? "秋季学期" : term === "2" ? "春季学期" : term;
          }
        }
        if (!semester) return "-";
        const parts = semester.split("-");
        if (parts.length >= 3) {
          const term = parts[2];
          return term === "1" ? "秋季学期" : term === "2" ? "春季学期" : term;
        } else if (semester.includes("1")) {
          return "秋季学期";
        } else if (semester.includes("2")) {
          return "春季学期";
        }
        return semester;
      },
      getSemesterType(semester) {
        const sem = this.semesterOptions.find(s => s.semesterValue === semester);
        if (sem) {
          if (sem.semesterValue.includes("1")) return "primary";
          if (sem.semesterValue.includes("2")) return "success";
          return "default";
        }
        if (!semester) return "default";
        if (semester.includes("1")) return "primary";
        if (semester.includes("2")) return "success";
        return "default";
      },
      isSelected(offeringId) {
        return this.selectedOfferings.some(id => id === offeringId || String(id) === String(offeringId));
      },
      getPercentage(selected, max) {
        if (max === 0) return '0.00';
        return ((selected / max) * 100).toFixed(2);
      }
    }
  };
</script>

<style scoped>
.select-closed-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.select-closed-content {
  text-align: center;
  padding: 40px;
}

.select-closed-content h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.select-closed-content p {
  font-size: 16px;
  color: #909399;
  margin: 0;
}
</style>
