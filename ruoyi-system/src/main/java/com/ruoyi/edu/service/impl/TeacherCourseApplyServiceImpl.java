package com.ruoyi.edu.service.impl;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.mapper.TeacherCourseApplyMapper;
import com.ruoyi.edu.service.ITeacherCourseApplyService;
import com.ruoyi.edu.dto.ApplyCommitDTO;
import com.ruoyi.edu.dto.ApplyRefuseDTO;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;

@Service
public class TeacherCourseApplyServiceImpl implements ITeacherCourseApplyService {

    @Autowired
    private TeacherCourseApplyMapper teacherCourseApplyMapper;

    @Override
    public List<TeacherCourseApply> selectTeacherCourseApplyList(TeacherCourseApply apply) {
        return teacherCourseApplyMapper.selectTeacherCourseApplyList(apply);
    }

    @Override
    public TeacherCourseApply selectTeacherCourseApplyById(Long id) {
        return teacherCourseApplyMapper.selectTeacherCourseApplyById(id);
    }

    @Override
    public int insertTeacherCourseApply(TeacherCourseApply apply) {
        apply.setStats("pending");
        apply.setCreateTime(DateUtils.getNowDate());
        return teacherCourseApplyMapper.insertTeacherCourseApply(apply);
    }

    @Override
    public int updateTeacherCourseApply(TeacherCourseApply apply) {
        apply.setUpdateTime(DateUtils.getNowDate());
        return teacherCourseApplyMapper.updateTeacherCourseApply(apply);
    }

    @Override
    public int deleteTeacherCourseApplyById(Long id) {
        return teacherCourseApplyMapper.deleteTeacherCourseApplyById(id);
    }

    @Override
    public int deleteTeacherCourseApplyByIds(Long[] ids) {
        return teacherCourseApplyMapper.deleteTeacherCourseApplyByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyCommit(ApplyCommitDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", dto.getId());
        params.put("courseNo", dto.getCourseNo());
        params.put("hours", dto.getHours());
        params.put("schedule", dto.getSchedule());
        params.put("location", dto.getLocation());
        params.put("maxCapacity", dto.getMaxCapacity());
        params.put("operator", "admin");
        params.put("outCode", 0);
        params.put("outMsg", "");

        teacherCourseApplyMapper.applyCommitByProcedure(params);

        Integer outCode = (Integer) params.get("outCode");
        String outMsg = (String) params.get("outMsg");
        if (outCode == null || outCode.intValue() != 0) {
            throw new ServiceException(outMsg != null ? outMsg : "审核失败");
        }
        return 1;
    }

    @Override
    public int applyRefuse(ApplyRefuseDTO dto) {
        TeacherCourseApply apply = teacherCourseApplyMapper.selectTeacherCourseApplyById(dto.getId());
        if (apply == null) {
            throw new ServiceException("申请不存在");
        }
        if (!"pending".equals(apply.getStats())) {
            throw new ServiceException("该申请已处理，无法重复审核");
        }

        TeacherCourseApply updateApply = new TeacherCourseApply();
        updateApply.setId(dto.getId());
        updateApply.setStats("rejected");
        updateApply.setRefuseReason(dto.getRefuseReason());
        updateApply.setUpdateBy("admin");
        updateApply.setUpdateTime(DateUtils.getNowDate());
        return teacherCourseApplyMapper.updateApplyStatus(updateApply);
    }
}
