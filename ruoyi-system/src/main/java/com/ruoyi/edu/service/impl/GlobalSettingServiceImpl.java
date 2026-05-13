package com.ruoyi.edu.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.edu.mapper.GlobalSettingMapper;
import com.ruoyi.edu.domain.GlobalSetting;
import com.ruoyi.edu.service.IGlobalSettingService;

/**
 * 全局设置Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class GlobalSettingServiceImpl implements IGlobalSettingService 
{
    @Autowired
    private GlobalSettingMapper globalSettingMapper;

    private static final String KEY_SELECT_OPEN = "is_select_open";
    private static final String KEY_SCORE_ENTRY_OPEN = "is_score_entry_open";

    /**
     * 查询全局设置
     * 
     * @param id 全局设置主键
     * @return 全局设置
     */
    @Override
    public GlobalSetting selectGlobalSettingById(Long id)
    {
        return globalSettingMapper.selectGlobalSettingById(id);
    }

    /**
     * 根据设置键查询
     * 
     * @param settingKey 设置键
     * @return 全局设置
     */
    @Override
    public GlobalSetting selectGlobalSettingByKey(String settingKey)
    {
        return globalSettingMapper.selectGlobalSettingByKey(settingKey);
    }

    /**
     * 查询所有设置，返回键值对
     * 
     * @return 设置映射
     */
    @Override
    public Map<String, String> getAllGlobalSettings()
    {
        Map<String, String> result = new HashMap<>();
        List<GlobalSetting> settings = globalSettingMapper.selectAllGlobalSettings();
        for (GlobalSetting setting : settings) {
            result.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return result;
    }

    /**
     * 查询全局设置列表
     * 
     * @param globalSetting 全局设置
     * @return 全局设置
     */
    @Override
    public List<GlobalSetting> selectGlobalSettingList(GlobalSetting globalSetting)
    {
        return globalSettingMapper.selectGlobalSettingList(globalSetting);
    }

    /**
     * 新增全局设置
     * 
     * @param globalSetting 全局设置
     * @return 结果
     */
    @Override
    @Transactional
    public int insertGlobalSetting(GlobalSetting globalSetting)
    {
        globalSetting.setCreateTime(DateUtils.getNowDate());
        return globalSettingMapper.insertGlobalSetting(globalSetting);
    }

    /**
     * 修改全局设置
     * 
     * @param globalSetting 全局设置
     * @return 结果
     */
    @Override
    @Transactional
    public int updateGlobalSetting(GlobalSetting globalSetting)
    {
        globalSetting.setUpdateTime(DateUtils.getNowDate());
        return globalSettingMapper.updateGlobalSetting(globalSetting);
    }

    /**
     * 批量删除全局设置
     * 
     * @param ids 需要删除的全局设置主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteGlobalSettingByIds(Long[] ids)
    {
        return globalSettingMapper.deleteGlobalSettingByIds(ids);
    }

    /**
     * 删除全局设置信息
     * 
     * @param id 全局设置主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteGlobalSettingById(Long id)
    {
        return globalSettingMapper.deleteGlobalSettingById(id);
    }

    /**
     * 更新或插入设置
     * 
     * @param settingKey 设置键
     * @param settingValue 设置值
     */
    @Override
    @Transactional
    public void updateSetting(String settingKey, String settingValue)
    {
        GlobalSetting existing = globalSettingMapper.selectGlobalSettingByKey(settingKey);
        if (existing == null) {
            GlobalSetting setting = new GlobalSetting();
            setting.setSettingKey(settingKey);
            setting.setSettingValue(settingValue);
            globalSettingMapper.insertGlobalSetting(setting);
        } else {
            existing.setSettingValue(settingValue);
            globalSettingMapper.updateGlobalSetting(existing);
        }
    }

    /**
     * 是否开启选课
     * 
     * @return true-开启，false-关闭
     */
    @Override
    public boolean isSelectOpen()
    {
        GlobalSetting setting = selectGlobalSettingByKey(KEY_SELECT_OPEN);
        return setting != null && "1".equals(setting.getSettingValue());
    }

    /**
     * 设置选课开关
     * 
     * @param open true-开启，false-关闭
     */
    @Override
    @Transactional
    public void setSelectOpen(boolean open)
    {
        updateSetting(KEY_SELECT_OPEN, open ? "1" : "0");
    }

    /**
     * 是否开启教师登分
     * 
     * @return true-开启，false-关闭
     */
    @Override
    public boolean isScoreEntryOpen()
    {
        GlobalSetting setting = selectGlobalSettingByKey(KEY_SCORE_ENTRY_OPEN);
        return setting != null && "1".equals(setting.getSettingValue());
    }

    /**
     * 设置教师登分开关
     * 
     * @param open true-开启，false-关闭
     */
    @Override
    @Transactional
    public void setScoreEntryOpen(boolean open)
    {
        updateSetting(KEY_SCORE_ENTRY_OPEN, open ? "1" : "0");
    }
}
