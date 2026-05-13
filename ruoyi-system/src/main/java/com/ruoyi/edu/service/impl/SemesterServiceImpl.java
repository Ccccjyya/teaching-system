package com.ruoyi.edu.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.edu.mapper.SemesterMapper;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.service.ISemesterService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;

/**
 * 学期Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class SemesterServiceImpl implements ISemesterService 
{
    @Autowired
    private SemesterMapper semesterMapper;

    /**
     * 查询学期
     * 
     * @param id 学期主键
     * @return 学期
     */
    @Override
    public Semester selectSemesterById(Long id)
    {
        return semesterMapper.selectSemesterById(id);
    }

    /**
     * 查询当前学期
     * 
     * @return 学期
     */
    @Override
    public Semester selectCurrentSemester()
    {
        return semesterMapper.selectCurrentSemester();
    }

    /**
     * 查询学期列表
     * 
     * @param semester 学期
     * @return 学期
     */
    @Override
    public List<Semester> selectSemesterList(Semester semester)
    {
        return semesterMapper.selectSemesterList(semester);
    }

    /**
     * 新增学期
     * 
     * @param semester 学期
     * @return 结果
     */
    @Override
    @Transactional
    public int insertSemester(Semester semester)
    {
        semester.setCreateTime(DateUtils.getNowDate());
        // 新增时默认为非当前学期
        if (semester.getIsCurrent() == null) {
            semester.setIsCurrent(0);
        }
        return semesterMapper.insertSemester(semester);
    }

    /**
     * 修改学期
     * 
     * @param semester 学期
     * @return 结果
     */
    @Override
    @Transactional
    public int updateSemester(Semester semester)
    {
        semester.setUpdateTime(DateUtils.getNowDate());
        return semesterMapper.updateSemester(semester);
    }

    /**
     * 批量删除学期
     * 
     * @param ids 需要删除的学期主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSemesterByIds(Long[] ids)
    {
        // 检查是否包含当前学期
        for (Long id : ids) {
            Semester semester = semesterMapper.selectSemesterById(id);
            if (semester != null && semester.getIsCurrent() != null && semester.getIsCurrent() == 1) {
                throw new ServiceException("不能删除当前学期");
            }
        }
        return semesterMapper.deleteSemesterByIds(ids);
    }

    /**
     * 删除学期信息
     * 
     * @param id 学期主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteSemesterById(Long id)
    {
        // 检查是否是当前学期
        Semester semester = semesterMapper.selectSemesterById(id);
        if (semester != null && semester.getIsCurrent() != null && semester.getIsCurrent() == 1) {
            throw new ServiceException("不能删除当前学期");
        }
        return semesterMapper.deleteSemesterById(id);
    }

    /**
     * 设置当前学期
     * 
     * @param id 学期ID
     */
    @Override
    @Transactional
    public void setCurrentSemester(Long id)
    {
        Semester semester = semesterMapper.selectSemesterById(id);
        if (semester == null) {
            throw new ServiceException("学期不存在");
        }
        if (semester.getIsCurrent() != null && semester.getIsCurrent() == 1) {
            throw new ServiceException("该学期已是当前学期");
        }

        // 将所有学期设为非当前
        semesterMapper.updateAllToNotCurrent();
        // 将指定学期设为当前
        semesterMapper.updateCurrentSemester(id);
    }
}
