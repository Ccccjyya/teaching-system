package com.ruoyi.edu.vo;

import com.ruoyi.common.annotation.Excel;

/**
 * 教师授课VO
 */
public class TeacherCourseVO {

    /**
     * 开课ID
     */
    private Long offeringId;

    /**
     * 学年学期
     */
    private String semester;

    /**
     * 课程号
     */
    @Excel(name = "课程号")
    private String courseNo;

    /**
     * 课程名称
     */
    @Excel(name = "课程名称")
    private String courseName;

    /**
     * 学分
     */
    @Excel(name = "学分")
    private Integer credit;

    /**
     * 学时
     */
    @Excel(name = "学时")
    private Integer hours;

    /**
     * 上课时间
     */
    @Excel(name = "上课时间")
    private String schedule;

    /**
     * 上课地点
     */
    @Excel(name = "上课地点")
    private String location;

    /**
     * 选课人数
     */
    @Excel(name = "选课人数")
    private Integer selectedCount;

    /**
     * 最大容量
     */
    @Excel(name = "最大容量")
    private Integer maxCapacity;

    /**
     * 是否需要登分（有学生但无成绩）
     */
    private Boolean needScoreEntry;

    public Long getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(Long offeringId) {
        this.offeringId = offeringId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(Integer selectedCount) {
        this.selectedCount = selectedCount;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Boolean getNeedScoreEntry() {
        return needScoreEntry;
    }

    public void setNeedScoreEntry(Boolean needScoreEntry) {
        this.needScoreEntry = needScoreEntry;
    }
}