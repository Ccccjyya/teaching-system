package com.ruoyi.edu.service;

import java.util.List;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.dto.ApplyCommitDTO;
import com.ruoyi.edu.dto.ApplyRefuseDTO;

public interface ITeacherCourseApplyService {

    public List<TeacherCourseApply> selectTeacherCourseApplyList(TeacherCourseApply apply);

    public TeacherCourseApply selectTeacherCourseApplyById(Long id);

    public int insertTeacherCourseApply(TeacherCourseApply apply);

    public int updateTeacherCourseApply(TeacherCourseApply apply);

    public int deleteTeacherCourseApplyById(Long id);

    public int deleteTeacherCourseApplyByIds(Long[] ids);

    public int applyCommit(ApplyCommitDTO dto);

    public int applyRefuse(ApplyRefuseDTO dto);
}