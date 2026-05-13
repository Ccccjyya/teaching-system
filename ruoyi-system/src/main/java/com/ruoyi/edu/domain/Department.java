package com.ruoyi.edu.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.Size;

public class Department extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Excel(name = "院系ID", cellType = ColumnType.NUMERIC)
    private Long deptId;

    @Excel(name = "院系编号")
    @Size(min = 0, max = 20, message = "院系编号长度不能超过20个字符")
    private String deptCode;

    @Excel(name = "院系名称")
    @Size(min = 0, max = 50, message = "院系名称长度不能超过50个字符")
    private String deptName;

    @Excel(name = "联系电话")
    @Size(min = 0, max = 20, message = "联系电话长度不能超过20个字符")
    private String phone;

    @Excel(name = "地址")
    @Size(min = 0, max = 100, message = "地址长度不能超过100个字符")
    private String address;

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("deptId", getDeptId())
                .append("deptCode", getDeptCode())
                .append("deptName", getDeptName())
                .append("phone", getPhone())
                .append("address", getAddress())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
