package com.ruoyi.edu.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 学期对象 edu_semester
 * 
 * @author ruoyi
 */
public class Semester extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学期ID */
    @Excel(name = "学期ID", cellType = ColumnType.NUMERIC)
    private Long id;

    /** 学期标识 */
    @Excel(name = "学期标识")
    @NotBlank(message = "学期标识不能为空")
    @Size(min = 0, max = 20, message = "学期标识长度不能超过20个字符")
    private String semesterValue;

    /** 学期名称 */
    @Excel(name = "学期名称")
    @NotBlank(message = "学期名称不能为空")
    @Size(min = 0, max = 100, message = "学期名称长度不能超过100个字符")
    private String semesterDesc;

    /** 是否当前学期（0否 1是） */
    @Excel(name = "是否当前学期", readConverterExp = "0=否,1=是")
    private Integer isCurrent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSemesterValue() {
        return semesterValue;
    }

    public void setSemesterValue(String semesterValue) {
        this.semesterValue = semesterValue;
    }

    public String getSemesterDesc() {
        return semesterDesc;
    }

    public void setSemesterDesc(String semesterDesc) {
        this.semesterDesc = semesterDesc;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("semesterValue", getSemesterValue())
                .append("semesterDesc", getSemesterDesc())
                .append("isCurrent", getIsCurrent())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
