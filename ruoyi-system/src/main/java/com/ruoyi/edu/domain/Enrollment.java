package com.ruoyi.edu.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 选课对象 edu_enrollment
 * 
 * @author ruoyi
 */
public class Enrollment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 选课ID */
    @Excel(name = "选课ID", cellType = ColumnType.NUMERIC)
    private Long enrollmentId;

    /** 开课ID */
    @NotNull(message = "开课记录不能为空")
    private Long offeringId;

    /** 开课对象 */
    @Excel(name = "课程名称", targetAttr = "course.courseName")
    private CourseOffering courseOffering;

    /** 学年 */
    @Excel(name = "学年")
    @NotBlank(message = "学年不能为空")
    @Size(min = 0, max = 20, message = "学年长度不能超过20个字符")
    private String academicYear;

    /** 学期 */
    @Excel(name = "学期")
    @NotBlank(message = "学期不能为空")
    @Size(min = 0, max = 20, message = "学期长度不能超过20个字符")
    private String semester;

    /** 学生ID */
    @NotNull(message = "学生不能为空")
    private Long studentId;

    /** 学生对象 */
    @Excel(name = "学生姓名", targetAttr = "studentName")
    private Student student;

    /** 课程号 */
    @Excel(name = "课程号")
    @NotBlank(message = "课程号不能为空")
    @Size(min = 0, max = 20, message = "课程号长度不能超过20个字符")
    private String courseNo;

    /** 教师ID */
    @NotNull(message = "教师不能为空")
    private Long teacherId;

    /** 教师对象 */
    @Excel(name = "授课教师", targetAttr = "teacherName")
    private Teacher teacher;

    /** 学生手机号 */
    @Excel(name = "学生手机号", cellType = ColumnType.TEXT)
    private String studentPhone;

    /** 学生院系名称 */
    @Excel(name = "学生院系")
    private String studentDeptName;

    /** 平时成绩 */
    @Excel(name = "平时成绩", scale = 1)
    private BigDecimal usualScore;

    /** 考试成绩 */
    @Excel(name = "考试成绩", scale = 1)
    private BigDecimal examScore;

    /** 总评成绩 */
    @Excel(name = "总评成绩", scale = 1)
    private BigDecimal totalScore;

    /** 状态（0正常 1已退课） */
    @Excel(name = "选课状态", readConverterExp = "0=正常,1=已退课")
    private String status;

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Long getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(Long offeringId) {
        this.offeringId = offeringId;
    }

    public CourseOffering getCourseOffering() {
        return courseOffering;
    }

    public void setCourseOffering(CourseOffering courseOffering) {
        this.courseOffering = courseOffering;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
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

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getStudentDeptName() {
        return studentDeptName;
    }

    public void setStudentDeptName(String studentDeptName) {
        this.studentDeptName = studentDeptName;
    }

    public BigDecimal getUsualScore() {
        return usualScore;
    }

    public void setUsualScore(BigDecimal usualScore) {
        this.usualScore = usualScore;
    }

    public BigDecimal getExamScore() {
        return examScore;
    }

    public void setExamScore(BigDecimal examScore) {
        this.examScore = examScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("enrollmentId", getEnrollmentId())
                .append("offeringId", getOfferingId())
                .append("academicYear", getAcademicYear())
                .append("semester", getSemester())
                .append("studentId", getStudentId())
                .append("courseNo", getCourseNo())
                .append("teacherId", getTeacherId())
                .append("usualScore", getUsualScore())
                .append("examScore", getExamScore())
                .append("totalScore", getTotalScore())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
