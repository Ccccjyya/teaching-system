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
 * 学生对象 edu_student
 * 
 * @author ruoyi
 */
public class Student extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生ID */
    @Excel(name = "学生ID", cellType = ColumnType.NUMERIC)
    private Long studentId;

    /** 学号 */
    @Excel(name = "学号")
    @NotBlank(message = "学号不能为空")
    @Size(min = 0, max = 20, message = "学号长度不能超过20个字符")
    private String studentNo;

    /** 姓名 */
    @Excel(name = "姓名")
    @NotBlank(message = "姓名不能为空")
    @Size(min = 0, max = 20, message = "姓名长度不能超过20个字符")
    private String studentName;

    /** 性别（0男 1女 2未知） */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String gender;

    /** 出生日期 */
    @Excel(name = "出生日期", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 籍贯 */
    @Excel(name = "籍贯")
    @Size(min = 0, max = 50, message = "籍贯长度不能超过50个字符")
    private String nativePlace;

    /** 手机号码 */
    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    private String phone;

    /** 院系ID */
    @NotNull(message = "所属院系不能为空")
    private Long deptId;

    /** 院系对象 */
    @Excel(name = "所属院系", targetAttr = "deptName")
    private Department dept;

    /** 简介 */
    @Size(min = 0, max = 500, message = "简介长度不能超过500个字符")
    private String introduction;

    /** 学分 */
    @Excel(name = "学分", cellType = ColumnType.NUMERIC)
    private Integer credit;

    /** 用户ID */
    private Long userId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public String getNativePlace() {
        return nativePlace;
    }

    public void setNativePlace(String nativePlace) {
        this.nativePlace = nativePlace;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
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
                .append("studentId", getStudentId())
                .append("studentNo", getStudentNo())
                .append("studentName", getStudentName())
                .append("gender", getGender())
                .append("birthday", getBirthday())
                .append("nativePlace", getNativePlace())
                .append("phone", getPhone())
                .append("deptId", getDeptId())
                .append("introduction", getIntroduction())
                .append("credit", getCredit())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
