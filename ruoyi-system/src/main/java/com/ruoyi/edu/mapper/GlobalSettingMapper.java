package com.ruoyi.edu.mapper;

import java.util.List;
import com.ruoyi.edu.domain.GlobalSetting;

/**
 * 全局设置Mapper接口
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
public interface GlobalSettingMapper 
{
    /**
     * 查询全局设置
     * 
     * @param id 全局设置主键
     * @return 全局设置
     */
    public GlobalSetting selectGlobalSettingById(Long id);

    /**
     * 根据设置键查询
     * 
     * @param settingKey 设置键
     * @return 全局设置
     */
    public GlobalSetting selectGlobalSettingByKey(String settingKey);

    /**
     * 查询所有设置
     * 
     * @return 全局设置集合
     */
    public List<GlobalSetting> selectAllGlobalSettings();

    /**
     * 查询全局设置列表
     * 
     * @param globalSetting 全局设置
     * @return 全局设置集合
     */
    public List<GlobalSetting> selectGlobalSettingList(GlobalSetting globalSetting);

    /**
     * 新增全局设置
     * 
     * @param globalSetting 全局设置
     * @return 结果
     */
    public int insertGlobalSetting(GlobalSetting globalSetting);

    /**
     * 修改全局设置
     * 
     * @param globalSetting 全局设置
     * @return 结果
     */
    public int updateGlobalSetting(GlobalSetting globalSetting);

    /**
     * 删除全局设置
     * 
     * @param id 全局设置主键
     * @return 结果
     */
    public int deleteGlobalSettingById(Long id);

    /**
     * 批量删除全局设置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGlobalSettingByIds(Long[] ids);
}
