package com.ruoyi.edu.mapper;

import java.util.List;
import com.ruoyi.edu.domain.Semester;

/**
 * 学期Mapper接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface SemesterMapper 
{
    /**
     * 查询学期
     * 
     * @param id 学期主键
     * @return 学期
     */
    public Semester selectSemesterById(Long id);

    /**
     * 查询当前学期
     * 
     * @return 学期
     */
    public Semester selectCurrentSemester();

    /**
     * 查询学期列表
     * 
     * @param semester 学期
     * @return 学期集合
     */
    public List<Semester> selectSemesterList(Semester semester);

    /**
     * 新增学期
     * 
     * @param semester 学期
     * @return 结果
     */
    public int insertSemester(Semester semester);

    /**
     * 修改学期
     * 
     * @param semester 学期
     * @return 结果
     */
    public int updateSemester(Semester semester);

    /**
     * 删除学期
     * 
     * @param id 学期主键
     * @return 结果
     */
    public int deleteSemesterById(Long id);

    /**
     * 批量删除学期
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSemesterByIds(Long[] ids);

    /**
     * 将所有学期设置为非当前学期
     * 
     * @return 结果
     */
    public int updateAllToNotCurrent();

    /**
     * 设置指定学期为当前学期
     * 
     * @param id 学期ID
     * @return 结果
     */
    public int updateCurrentSemester(Long id);
}
