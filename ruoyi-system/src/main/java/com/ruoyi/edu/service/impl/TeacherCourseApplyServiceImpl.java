package com.ruoyi.edu.service.impl;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.domain.TeacherCourseApply;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.mapper.TeacherCourseApplyMapper;
import com.ruoyi.edu.service.ITeacherCourseApplyService;
import com.ruoyi.edu.dto.ApplyCommitDTO;
import com.ruoyi.edu.dto.ApplyRefuseDTO;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class TeacherCourseApplyServiceImpl implements ITeacherCourseApplyService {

    @Autowired
    private TeacherCourseApplyMapper teacherCourseApplyMapper;

    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

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
        apply.setSchedule(formatScheduleAsTimeRange(apply.getSchedule()));
        return teacherCourseApplyMapper.insertTeacherCourseApply(apply);
    }

    @Override
    public int updateTeacherCourseApply(TeacherCourseApply apply) {
        apply.setUpdateTime(DateUtils.getNowDate());
        apply.setSchedule(formatScheduleAsTimeRange(apply.getSchedule()));
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
        TeacherCourseApply apply = teacherCourseApplyMapper.selectTeacherCourseApplyById(dto.getId());
        if (apply == null) {
            throw new ServiceException("申请不存在");
        }
        if (!"pending".equals(apply.getStats())) {
            throw new ServiceException("该申请已处理，无法重复审核");
        }
        if (apply.getCourseId() != null) {
            dto.setCourseNo(apply.getCourseNo());
            dto.setHours(null);
        } else if (StringUtils.isNotBlank(dto.getCourseNo())) {
            dto.setCourseNo(dto.getCourseNo().trim());
        }

        String normalizedSchedule = formatScheduleAsTimeRange(dto.getSchedule());
        checkLocationScheduleConflict(apply.getXq(), dto.getLocation(), normalizedSchedule);

        Map<String, Object> params = new HashMap<>();
        params.put("id", dto.getId());
        params.put("courseNo", dto.getCourseNo());
        params.put("hours", dto.getHours());
        params.put("schedule", normalizedSchedule);
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

    private String formatScheduleAsTimeRange(String schedule) {
        if (StringUtils.isBlank(schedule)) {
            return schedule;
        }
        String[] parts = schedule.split("[,，;；]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String item = part.trim();
            String normalized = normalizeSchedulePart(item);
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(normalized);
        }
        return builder.toString();
    }

    private String normalizeSchedulePart(String schedulePart) {
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (String day : days) {
            if (schedulePart.startsWith(day)) {
                String period = schedulePart.substring(day.length()).trim();
                String timeRange = convertPeriodToTimeRange(period);
                if (timeRange != null) {
                    return day + " " + timeRange;
                }
                return schedulePart;
            }
        }
        return schedulePart;
    }

    private String convertPeriodToTimeRange(String period) {
        if (period == null) {
            return null;
        }
        switch (period.replace(" ", "")) {
            case "1-2节": return "8:00-9:40";
            case "3-4节": return "10:00-11:40";
            case "5-6节": return "14:00-15:40";
            case "7-8节": return "16:00-17:40";
            case "9-10节": return "19:00-20:40";
            default: return null;
        }
    }

    private void checkLocationScheduleConflict(String semester, String location, String schedule) {
        if (StringUtils.isAnyBlank(semester, location, schedule)) {
            return;
        }
        List<TimeSlot> newSlots = parseSchedule(schedule);
        if (newSlots.isEmpty()) {
            throw new ServiceException("上课时间格式无法识别，请选择有效的星期和时间段");
        }
        List<CourseOffering> existingOfferings = courseOfferingMapper.selectLocationOfferingsForConflict(semester, location, null);
        for (CourseOffering existingOffering : existingOfferings) {
            List<TimeSlot> existingSlots = parseSchedule(existingOffering.getSchedule());
            for (TimeSlot newSlot : newSlots) {
                for (TimeSlot existingSlot : existingSlots) {
                    if (newSlot.conflictsWith(existingSlot)) {
                        throw new ServiceException("该上课地点在当前学期该时间段已被占用，不能重复开课");
                    }
                }
            }
        }
    }

    private List<TimeSlot> parseSchedule(String schedule) {
        List<TimeSlot> slots = new ArrayList<>();
        if (StringUtils.isBlank(schedule)) {
            return slots;
        }
        String[] parts = schedule.split("[,，;；]");
        Pattern sectionPattern = Pattern.compile("(周[一二三四五六日])\\s*(\\d+)\\s*-\\s*(\\d+)\\s*节");
        Pattern timePattern = Pattern.compile("(周[一二三四五六日])\\s*([0-2]?\\d:[0-5]\\d)\\s*-\\s*([0-2]?\\d:[0-5]\\d)");
        for (String part : parts) {
            String item = part.trim();
            Matcher sectionMatcher = sectionPattern.matcher(item);
            if (sectionMatcher.matches()) {
                int startSection = Integer.parseInt(sectionMatcher.group(2));
                int endSection = Integer.parseInt(sectionMatcher.group(3));
                slots.add(new TimeSlot(convertDayToNumber(sectionMatcher.group(1)), startSection, endSection));
                continue;
            }
            Matcher timeMatcher = timePattern.matcher(item);
            if (timeMatcher.matches()) {
                int startMinute = convertTimeToMinutes(timeMatcher.group(2));
                int endMinute = convertTimeToMinutes(timeMatcher.group(3));
                int[] sections = convertMinutesToSections(startMinute, endMinute);
                if (sections[0] > 0 && sections[1] > 0) {
                    slots.add(new TimeSlot(convertDayToNumber(timeMatcher.group(1)), sections[0], sections[1]));
                }
            }
        }
        return slots;
    }

    private int convertDayToNumber(String day) {
        switch (day) {
            case "周一": return 1;
            case "周二": return 2;
            case "周三": return 3;
            case "周四": return 4;
            case "周五": return 5;
            case "周六": return 6;
            case "周日": return 7;
            default: return 0;
        }
    }

    private int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private int[] convertMinutesToSections(int startMinute, int endMinute) {
        return new int[] { convertMinuteToSection(startMinute), convertMinuteToSection(endMinute) };
    }

    private int convertMinuteToSection(int minute) {
        if (minute < 10 * 60) {
            return minute < 9 * 60 ? 1 : 2;
        }
        if (minute < 12 * 60) {
            return minute < 11 * 60 ? 3 : 4;
        }
        if (minute < 16 * 60) {
            return minute < 15 * 60 ? 5 : 6;
        }
        if (minute < 18 * 60) {
            return minute < 17 * 60 ? 7 : 8;
        }
        if (minute < 21 * 60) {
            return minute < 20 * 60 ? 9 : 10;
        }
        return 0;
    }

    private static class TimeSlot {
        private final int day;
        private final int startSection;
        private final int endSection;

        private TimeSlot(int day, int startSection, int endSection) {
            this.day = day;
            this.startSection = Math.min(startSection, endSection);
            this.endSection = Math.max(startSection, endSection);
        }

        private boolean conflictsWith(TimeSlot other) {
            return day == other.day && startSection <= other.endSection && endSection >= other.startSection;
        }
    }
}
