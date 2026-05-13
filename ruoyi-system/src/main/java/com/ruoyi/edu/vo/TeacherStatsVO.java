package com.ruoyi.edu.vo;

/**
 * 教师统计VO
 */
public class TeacherStatsVO {

    /**
     * 本学期授课门数
     */
    private Integer courseCount;

    /**
     * 总学生人数
     */
    private Integer studentCount;

    /**
     * 待登分课程数
     */
    private Integer pendingScoreCount;

    /**
     * 当前学期
     */
    private String currentSemester;

    public Integer getCourseCount() {
        return courseCount;
    }

    public void setCourseCount(Integer courseCount) {
        this.courseCount = courseCount;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getPendingScoreCount() {
        return pendingScoreCount;
    }

    public void setPendingScoreCount(Integer pendingScoreCount) {
        this.pendingScoreCount = pendingScoreCount;
    }

    public String getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(String currentSemester) {
        this.currentSemester = currentSemester;
    }
}