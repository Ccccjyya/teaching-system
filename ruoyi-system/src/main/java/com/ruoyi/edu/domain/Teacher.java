package com.ruoyi.edu.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 教师对象 edu_teacher
 * 
 * @author ruoyi
 */
public class Teacher extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 教师ID */
    @Excel(name = "教师ID", cellType = ColumnType.NUMERIC)
    private Long teacherId;

    /** 工号 */
    @Excel(name = "工号")
    @NotBlank(message = "工号不能为空")
    @Size(min = 0, max = 20, message = "工号长度不能超过20个字符")
    private String teacherNo;

    /** 姓名 */
    @Excel(name = "姓名")
    @NotBlank(message = "姓名不能为空")
    @Size(min = 0, max = 20, message = "姓名长度不能超过20个字符")
    private String teacherName;

    /** 性别（0男 1女 2未知） */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String gender;

    /** 出生日期 */
    @Excel(name = "出生日期", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 学历 */
    @Excel(name = "学历")
    @Size(min = 0, max = 20, message = "学历长度不能超过20个字符")
    private String education;

    /** 职称 */
    @Excel(name = "职称")
    @Size(min = 0, max = 20, message = "职称长度不能超过20个字符")
    private String title;

    /** 院系ID */
    @NotNull(message = "所属院系不能为空")
    private Long deptId;

    /** 院系对象 */
    @Excel(name = "所属院系", targetAttr = "deptName")
    private Department dept;

    /** 简介 */
    @Size(min = 0, max = 500, message = "简介长度不能超过500个字符")
    private String introduction;

    /** 用户ID */
    private Long userId;

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Department getDept() {
        return dept;
    }

    public void setDept(Department dept) {
        this.dept = dept;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("teacherId", getTeacherId())
                .append("teacherNo", getTeacherNo())
                .append("teacherName", getTeacherName())
                .append("gender", getGender())
                .append("birthday", getBirthday())
                .append("education", getEducation())
                .append("title", getTitle())
                .append("deptId", getDeptId())
                .append("introduction", getIntroduction())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
