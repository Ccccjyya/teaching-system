package com.ruoyi.edu.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.annotation.Excel.ColumnType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import com.ruoyi.common.annotation.Excel;

public class TeacherCourseApply extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Excel(name = "申请ID", cellType = ColumnType.NUMERIC)
    private Long id;

    @Excel(name = "学期")
    @NotBlank(message = "学期不能为空")
    @Size(min = 0, max = 20, message = "学期长度不能超过20个字符")
    private String xq;

    @Excel(name = "课程名称")
    @NotBlank(message = "课程名不能为空")
    @Size(min = 0, max = 100, message = "课程名长度不能超过100个字符")
    private String km;

    private Long courseId;

    @Excel(name = "课程号")
    private String courseNo;

    @Min(value = 0, message = "院系ID必须大于等于0")
    @Excel(name = "院系ID", cellType = ColumnType.NUMERIC)
    private Long yxhId;

    @Excel(name = "学分", scale = 1)
    @DecimalMin(value = "0.0", message = "学分必须大于等于0")
    private Double xf;

    @Excel(name = "教师工号")
    @NotBlank(message = "工号不能为空")
    @Size(min = 0, max = 20, message = "工号长度不能超过20个字符")
    private String gh;

    @Excel(name = "审核状态", readConverterExp = "pending=待审核,approved=通过,rejected=拒绝")
    private String stats;

    @Excel(name = "拒绝原因")
    private String refuseReason;

    @Excel(name = "院系名称")
    private String yxm;

    @Excel(name = "教师姓名")
    private String teacherName;
    
    @Excel(name = "学时", cellType = ColumnType.NUMERIC)
    private Integer xs;
    
    @Excel(name = "期望上课时间")
    @Size(min = 0, max = 50, message = "期望上课时间长度不能超过50个字符")
    private String schedule;

    @Excel(name = "希望容量")
    @Min(value = 1, message = "希望容量必须大于0")
    private Integer expectedCapacity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getXq() {
        return xq;
    }

    public void setXq(String xq) {
        this.xq = xq;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

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

    public Long getYxhId() {
        return yxhId;
    }

    public void setYxhId(Long yxhId) {
        this.yxhId = yxhId;
    }

    public Double getXf() {
        return xf;
    }

    public void setXf(Double xf) {
        this.xf = xf;
    }

    public String getGh() {
        return gh;
    }

    public void setGh(String gh) {
        this.gh = gh;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }

    public String getYxm() {
        return yxm;
    }

    public void setYxm(String yxm) {
        this.yxm = yxm;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Integer getXs() {
        return xs;
    }

    public void setXs(Integer xs) {
        this.xs = xs;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public Integer getExpectedCapacity() {
        return expectedCapacity;
    }

    public void setExpectedCapacity(Integer expectedCapacity) {
        this.expectedCapacity = expectedCapacity;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("xq", getXq())
                .append("km", getKm())
                .append("courseNo", getCourseNo())
                .append("yxhId", getYxhId())
                .append("xf", getXf())
                .append("gh", getGh())
                .append("stats", getStats())
                .append("refuseReason", getRefuseReason())
                .append("yxm", getYxm())
                .append("teacherName", getTeacherName())
                .append("expectedCapacity", getExpectedCapacity())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
