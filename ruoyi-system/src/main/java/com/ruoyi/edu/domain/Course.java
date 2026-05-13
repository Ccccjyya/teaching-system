package com.ruoyi.edu.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public class Course extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Excel(name = "课程ID", cellType = ColumnType.NUMERIC)
    private Long courseId;

    @Excel(name = "课程号")
    @NotBlank(message = "课程号不能为空")
    @Size(min = 0, max = 20, message = "课程号长度不能超过20个字符")
    private String courseNo;

    @Excel(name = "课程名称")
    @NotBlank(message = "课程名不能为空")
    @Size(min = 0, max = 50, message = "课程名长度不能超过50个字符")
    private String courseName;

    @Excel(name = "学分", cellType = ColumnType.NUMERIC)
    @Min(value = 0, message = "学分必须大于等于0")
    private Integer credit;

    @Excel(name = "学时", cellType = ColumnType.NUMERIC)
    @Min(value = 0, message = "学时必须大于等于0")
    private Integer hours;

    private Long deptId;

    @Excel(name = "开课院系")
    private String deptName;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("courseId", getCourseId())
                .append("courseNo", getCourseNo())
                .append("courseName", getCourseName())
                .append("credit", getCredit())
                .append("hours", getHours())
                .append("deptId", getDeptId())
                .append("deptName", getDeptName())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
