<template>
  <div class="app-container">
    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
    </el-row>

    <!-- 已选课程列表 -->
    <el-table v-loading="loading" :data="courseList" :fit="true" border>
      <el-table-column type="expand">
        <template slot-scope="props">
          <el-form label-position="left" inline class="table-expand">
            <el-form-item label="上课时间：">
              <span>{{ props.row.courseOffering.schedule }}</span>
            </el-form-item>
            <el-form-item label="上课地点：">
              <span>{{ props.row.courseOffering.location || '-' }}</span>
            </el-form-item>
            <el-form-item label="课程学分：">
              <span>{{ props.row.courseOffering.course.credit }}</span>
            </el-form-item>
          </el-form>
        </template>
      </el-table-column>
      <el-table-column label="课号" align="center" prop="courseNo" width="80" />
      <el-table-column label="课程名称" align="center" prop="courseOffering.course.courseName" min-width="180" />
      <el-table-column label="学期" align="center" width="130">
        <template slot-scope="scope">
          <div>{{ getYearDisplay(scope.row) }}</div>
          <el-tag size="mini" :type="getSemesterType(getSemesterValue(scope.row))">
            {{ getSemesterOnly(getSemesterValue(scope.row)) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="授课教师" align="center" prop="courseOffering.teacher.teacherName" width="100" />
      <el-table-column label="学分" align="center" prop="courseOffering.course.credit" width="70" />
      <el-table-column label="上课时间" align="center" prop="courseOffering.schedule" min-width="150" />
      <el-table-column label="状态" align="center" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
            {{ scope.row.status === '0' ? '已选' : '已退' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="danger"
            icon="el-icon-delete"
            :disabled="scope.row.status !== '0'"
            @click="handleDrop(scope.row)"
          >退课</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { myEnrollments, studentDrop } from "@/api/edu/enrollment";

export default {
  name: "MyCourses",
  data() {
    return {
      loading: true,
      courseList: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      myEnrollments().then(response => {
        this.courseList = response.data;
        this.loading = false;
      });
    },
    handleDrop(row) {
      this.$modal.confirm('确认退掉课程"' + row.courseOffering.course.courseName + '"吗？').then(() => {
        return studentDrop(row.enrollmentId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("退课成功！");
      }).catch(() => {});
    },
    getAcademicYear(semester) {
      if (!semester) return '';
      if (semester.length > 3) {
        return semester.substring(0, 9);
      }
      return '';
    },
    getSemesterValue(row) {
      if (row.courseOffering && row.courseOffering.semester) {
        return row.courseOffering.semester;
      }
      if (row.academicYear && row.semester) {
        return row.academicYear + '-' + row.semester;
      }
      return null;
    },
    getYearDisplay(row) {
      const semesterValue = this.getSemesterValue(row);
      if (semesterValue) {
        const year = this.getAcademicYear(semesterValue);
        if (year) return year;
      }
      if (row.academicYear) return row.academicYear;
      return '';
    },
    getSemesterOnly(semester) {
      if (!semester) return '';
      if (semester.length > 3) {
        return semester.endsWith('1') ? '秋季学期' : '春季学期';
      }
      return semester === '1' ? '秋季学期' : semester === '2' ? '春季学期' : '';
    },
    getSemesterType(semester) {
      if (!semester) return 'default';
      const isAutumn = semester.endsWith('1');
      return isAutumn ? 'primary' : 'success';
    }
  }
};
</script>