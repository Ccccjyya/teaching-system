package com.ruoyi.edu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApplyRefuseDTO {

    @NotNull(message = "申请ID不能为空")
    private Long id;

    @NotBlank(message = "拒绝原因不能为空")
    @Size(min = 1, max = 500, message = "拒绝原因长度不能超过500个字符")
    private String refuseReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }
}