package com.ruoyi.edu.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 开课对象 edu_course_offering
 * 
 * @author ruoyi
 */
public class CourseOffering extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 开课ID */
    @Excel(name = "开课ID", cellType = ColumnType.NUMERIC)
    private Long offeringId;

    /** 课程ID */
    @NotNull(message = "课程不能为空")
    private Long courseId;

    /** 课程对象 */
    @Excel(name = "课程名称", targetAttr = "courseName")
    private Course course;

    /** 课程号 */
    @Excel(name = "课程号")
    @Size(min = 0, max = 20, message = "课程号长度不能超过20个字符")
    private String courseNo;

    /** 课程名称（用于查询） */
    private String courseName;

    /** 星期几（用于查询过滤） */
    private String weekday;

    /** 时间段（用于查询过滤） */
    private String timePeriod;

    /** 是否已满（用于查询过滤：1-已满 0-未满） */
    private String fullStatus;

    /** 学期（用于查询过滤） */
    @Excel(name = "学期")
    private String semester;

    /** 教师ID */
    @NotNull(message = "教师不能为空")
    private Long teacherId;

    /** 教师对象 */
    @Excel(name = "授课教师", targetAttr = "teacherName")
    private Teacher teacher;

    /** 上课时间 */
    @Excel(name = "上课时间")
    @NotBlank(message = "上课时间不能为空")
    @Size(min = 0, max = 50, message = "上课时间长度不能超过50个字符")
    private String schedule;

    /** 上课地点 */
    @Excel(name = "上课地点")
    @Size(min = 0, max = 50, message = "上课地点长度不能超过50个字符")
    private String location;

    /** 最大容量 */
    @Excel(name = "最大容量", cellType = ColumnType.NUMERIC)
    private Integer maxCapacity;

    /** 已选人数 */
    @Excel(name = "已选人数", cellType = ColumnType.NUMERIC)
    private Integer selectedCount;

    public Long getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(Long offeringId) {
        this.offeringId = offeringId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getFullStatus() {
        return fullStatus;
    }

    public void setFullStatus(String fullStatus) {
        this.fullStatus = fullStatus;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
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

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Integer getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(Integer selectedCount) {
        this.selectedCount = selectedCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("offeringId", getOfferingId())
                .append("courseId", getCourseId())
                .append("courseNo", getCourseNo())
                .append("teacherId", getTeacherId())
                .append("schedule", getSchedule())
                .append("location", getLocation())
                .append("maxCapacity", getMaxCapacity())
                .append("selectedCount", getSelectedCount())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
