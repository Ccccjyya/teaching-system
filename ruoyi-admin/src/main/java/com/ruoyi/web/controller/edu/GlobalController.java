package com.ruoyi.web.controller.edu;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.edu.domain.GlobalSetting;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.service.IGlobalSettingService;
import com.ruoyi.edu.service.ISemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/edu/global")
@Validated
public class GlobalController extends BaseController {

    @Autowired
    private ISemesterService semesterService;

    @Autowired
    private IGlobalSettingService globalSettingService;

    // ==================== 学期管理 ====================

    @GetMapping("/semester/current")
    public AjaxResult getCurrentSemester() {
        return success(semesterService.selectCurrentSemester());
    }

    @PreAuthorize("@ss.hasRole('student') or @ss.hasPermi('edu:semester:list')")
    @GetMapping("/semester/list")
    public TableDataInfo listSemester(Semester semester) {
        startPage();
        List<Semester> list = semesterService.selectSemesterList(semester);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('edu:semester:query')")
    @GetMapping("/semester/{id}")
    public AjaxResult getSemesterInfo(@PathVariable Long id) {
        return success(semesterService.selectSemesterById(id));
    }

    @PreAuthorize("@ss.hasPermi('edu:semester:add')")
    @Log(title = "学期管理", businessType = BusinessType.INSERT)
    @PostMapping("/semester")
    public AjaxResult addSemester(@Validated @RequestBody Semester semester) {
        semester.setCreateBy(getUsername());
        return toAjax(semesterService.insertSemester(semester));
    }

    @PreAuthorize("@ss.hasPermi('edu:semester:edit')")
    @Log(title = "学期管理", businessType = BusinessType.UPDATE)
    @PutMapping("/semester")
    public AjaxResult editSemester(@Validated @RequestBody Semester semester) {
        semester.setUpdateBy(getUsername());
        return toAjax(semesterService.updateSemester(semester));
    }

    @PreAuthorize("@ss.hasPermi('edu:semester:remove')")
    @Log(title = "学期管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/semester/{ids}")
    public AjaxResult removeSemester(@PathVariable Long[] ids) {
        return toAjax(semesterService.deleteSemesterByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('edu:semester:switch')")
    @Log(title = "设置当前学期", businessType = BusinessType.UPDATE)
    @PostMapping("/semester/setCurrent/{id}")
    public AjaxResult setCurrentSemester(@PathVariable Long id) {
        semesterService.setCurrentSemester(id);
        return success();
    }

    // ==================== 全局设置 ====================

    @GetMapping("/settings")
    public AjaxResult getGlobalSettings() {
        Map<String, String> settings = globalSettingService.getAllGlobalSettings();
        Map<String, Object> result = new HashMap<>();
        result.put("is_select_open", "1".equals(settings.get("is_select_open")));
        result.put("is_score_entry_open", "1".equals(settings.get("is_score_entry_open")));
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('edu:global:selectOpen')")
    @Log(title = "全局设置", businessType = BusinessType.UPDATE)
    @PutMapping("/settings/selectOpen")
    public AjaxResult setSelectOpen(@RequestParam Boolean open) {
        globalSettingService.setSelectOpen(open);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('edu:global:scoreOpen')")
    @Log(title = "全局设置", businessType = BusinessType.UPDATE)
    @PutMapping("/settings/scoreEntryOpen")
    public AjaxResult setScoreEntryOpen(@RequestParam Boolean open) {
        globalSettingService.setScoreEntryOpen(open);
        return success();
    }
}
