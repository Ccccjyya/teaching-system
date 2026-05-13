package com.ruoyi.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SemesterSwitchDTO {

    @NotBlank(message = "新学期不能为空")
    @Size(min = 0, max = 20, message = "学期长度不能超过20个字符")
    private String newSemester;

    public String getNewSemester() {
        return newSemester;
    }

    public void setNewSemester(String newSemester) {
        this.newSemester = newSemester;
    }
}