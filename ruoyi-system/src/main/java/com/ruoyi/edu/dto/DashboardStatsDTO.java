package com.ruoyi.edu.dto;

public class DashboardStatsDTO {
    private Integer studentCount;
    private Integer teacherCount;
    private Integer courseCount;
    private Integer applyCount;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(Integer studentCount, Integer teacherCount, Integer courseCount, Integer applyCount) {
        this.studentCount = studentCount;
        this.teacherCount = teacherCount;
        this.courseCount = courseCount;
        this.applyCount = applyCount;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(Integer teacherCount) {
        this.teacherCount = teacherCount;
    }

    public Integer getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Integer courseCount) {
        this.courseCount = courseCount;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}