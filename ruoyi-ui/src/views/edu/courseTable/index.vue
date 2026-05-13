<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" class="mb8">
      <el-form-item label="学年">
        <el-select v-model="queryParams.academicYear" placeholder="请选择学年" clearable>
          <el-option 
            v-for="year in getAcademicYearOptions()" 
            :key="year.value" 
            :label="year.label" 
            :value="year.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学期">
        <el-select v-model="queryParams.semester" placeholder="请选择学期" clearable>
          <el-option 
            v-for="sem in getSemesterOptions()" 
            :key="sem.value" 
            :label="sem.label" 
            :value="sem.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card title="我的课表">
      <div class="schedule-container">
        <div class="schedule-header">
          <div class="time-col"></div>
          <div v-for="day in weekDays" :key="day.value" class="day-col">
            {{ day.label }}
          </div>
        </div>
        
        <div class="schedule-body">
          <div v-for="period in periods" :key="period" class="schedule-row">
            <div class="time-col">{{ period }}节</div>
            <div v-for="day in weekDays" :key="day.value" class="day-col">
              <div 
                v-for="course in getCoursesAt(day.value, period)" 
                :key="course.enrollmentId"
                class="course-block"
                :style="{ 
                  backgroundColor: course.color,
                  height: getCourseHeight(course) + 'px'
                }"
                @click="showCourseDetail(course)"
              >
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-teacher">{{ course.teacherName }}</div>
                <div class="course-location" v-if="course.location">{{ course.location }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card v-if="unscheduledCourses.length > 0" class="mt8" title="未排入课表课程">
      <el-table :data="unscheduledCourses" border size="small">
        <el-table-column label="课号" align="center" prop="courseNo" width="100" />
        <el-table-column label="课程名称" align="center" prop="courseName" />
        <el-table-column label="授课教师" align="center" prop="teacherName" width="120" />
        <el-table-column label="上课时间" align="center" prop="schedule" width="180" />
        <el-table-column label="上课地点" align="center" prop="location" width="140" />
      </el-table>
    </el-card>

    <el-dialog title="课程详情" :visible.sync="showDetail" width="400px">
      <el-form v-if="selectedCourse" label-width="100px">
        <el-form-item label="课程名称">
          <span>{{ selectedCourse.courseName }}</span>
        </el-form-item>
        <el-form-item label="课程编号">
          <span>{{ selectedCourse.courseNo }}</span>
        </el-form-item>
        <el-form-item label="授课教师">
          <span>{{ selectedCourse.teacherName }}</span>
        </el-form-item>
        <el-form-item label="上课时间">
          <span>{{ selectedCourse.schedule }}</span>
        </el-form-item>
        <el-form-item label="上课地点">
          <span>{{ selectedCourse.location || '-' }}</span>
        </el-form-item>
        <el-form-item label="学分">
          <span>{{ selectedCourse.credit }} 学分</span>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { queryCourseTable } from "@/api/edu/courseTable";
import { listSemester, getCurrentSemester } from "@/api/edu/global";

export default {
  name: "CourseTable",
  data() {
    return {
      loading: true,
      courseList: [],
      showDetail: false,
      selectedCourse: null,
      semesterList: [],
      queryParams: {
        academicYear: undefined,
        semester: undefined
      },
      weekDays: [
        { value: 1, label: '周一' },
        { value: 2, label: '周二' },
        { value: 3, label: '周三' },
        { value: 4, label: '周四' },
        { value: 5, label: '周五' },
        { value: 6, label: '周六' },
        { value: 7, label: '周日' }
      ],
      periods: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    };
  },
  created() {
    this.getSemesterList();
  },
  computed: {
    scheduledCourses() {
      return this.courseList.filter(course => course.dayOfWeek && course.startPeriod && course.endPeriod);
    },
    unscheduledCourses() {
      return this.courseList.filter(course => !course.dayOfWeek || !course.startPeriod || !course.endPeriod);
    }
  },
  methods: {
    getSemesterList() {
      listSemester({}).then(response => {
        this.semesterList = (response.rows || []).slice().sort((a, b) => {
          return b.semesterValue.localeCompare(a.semesterValue);
        });
      }).catch(() => {
        this.semesterList = [];
      }).finally(() => {
        this.setCurrentSemesterAndQuery();
      });
    },
    setCurrentSemesterAndQuery() {
      getCurrentSemester().then(response => {
        if (response.data && response.data.semesterValue) {
          const parts = response.data.semesterValue.split('-');
          if (parts.length >= 3) {
            this.queryParams.academicYear = parts[0] + '-' + parts[1];
            this.queryParams.semester = parts[2];
          }
        }
      }).finally(() => {
        this.getList();
      });
    },
    getList() {
      this.loading = true;
      queryCourseTable(this.queryParams).then(response => {
        this.courseList = response.data || [];
      }).catch(() => {
        this.courseList = [];
      }).finally(() => {
        this.loading = false;
      });
    },
    handleQuery() {
      this.getList();
    },
    resetQuery() {
      this.setCurrentSemesterAndQuery();
    },
    getCoursesAt(dayOfWeek, period) {
      return this.scheduledCourses.filter(course => {
        return course.dayOfWeek === dayOfWeek && 
               period >= course.startPeriod && 
               period <= course.endPeriod &&
               period === course.startPeriod;
      });
    },
    getCourseHeight(course) {
      if (!course.startPeriod || !course.endPeriod) return 50;
      const heightPerPeriod = 50;
      return (course.endPeriod - course.startPeriod + 1) * heightPerPeriod - 4;
    },
    showCourseDetail(course) {
      this.selectedCourse = course;
      this.showDetail = true;
    },
    getAcademicYearOptions() {
      const years = new Set();
      this.semesterList.forEach(s => {
        const parts = s.semesterValue.split('-');
        if (parts.length >= 2) {
          years.add(parts[0] + '-' + parts[1]);
        }
      });
      return Array.from(years).sort().map(year => ({ label: year, value: year }));
    },
    getSemesterOptions() {
      if (!this.queryParams.academicYear) return [];
      return this.semesterList
        .filter(s => s.semesterValue.startsWith(this.queryParams.academicYear))
        .map(s => {
          const parts = s.semesterValue.split('-');
          const sem = parts[2];
          return {
            label: sem === '1' ? '秋季学期' : sem === '2' ? '春季学期' : sem,
            value: sem
          };
        });
    }
  }
};
</script>

<style scoped>
.schedule-container {
  display: flex;
  flex-direction: column;
  border: 1px solid #e8e8e8;
}
.schedule-header {
  display: flex;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}
.schedule-body {
  display: flex;
  flex-direction: column;
}
.schedule-row {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
}
.time-col {
  width: 60px;
  padding: 8px;
  text-align: center;
  background: #fafafa;
  border-right: 1px solid #e8e8e8;
  font-size: 12px;
  color: #666;
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.day-col {
  flex: 1;
  padding: 2px;
  min-height: 50px;
  border-right: 1px solid #f0f0f0;
  position: relative;
}
.day-col:last-child {
  border-right: none;
}
.course-block {
  margin: 2px 0;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  position: absolute;
  left: 2px;
  right: 2px;
  top: 2px;
}
.course-name {
  font-size: 12px;
  font-weight: bold;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.course-teacher {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.course-location {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mt8 {
  margin-top: 8px;
}
</style>
