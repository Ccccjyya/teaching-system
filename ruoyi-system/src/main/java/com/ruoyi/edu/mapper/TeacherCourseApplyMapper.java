package com.ruoyi.edu.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.edu.domain.TeacherCourseApply;

public interface TeacherCourseApplyMapper {

    public List<TeacherCourseApply> selectTeacherCourseApplyList(TeacherCourseApply apply);

    public Long selectTeacherCourseApplyListCount(TeacherCourseApply apply);

    public TeacherCourseApply selectTeacherCourseApplyById(Long id);

    public int insertTeacherCourseApply(TeacherCourseApply apply);

    public int updateTeacherCourseApply(TeacherCourseApply apply);

    public int deleteTeacherCourseApplyById(Long id);

    public int deleteTeacherCourseApplyByIds(Long[] ids);

    public int updateApplyStatus(TeacherCourseApply apply);

    public void applyCommitByProcedure(Map<String, Object> params);
}
