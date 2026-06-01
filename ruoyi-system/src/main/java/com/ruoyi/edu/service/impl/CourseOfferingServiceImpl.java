package com.ruoyi.edu.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.edu.domain.Semester;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.mapper.EnrollmentMapper;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.service.ICourseOfferingService;
import com.ruoyi.edu.service.ISemesterService;

/**
 * 开课Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class CourseOfferingServiceImpl implements ICourseOfferingService 
{
    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private ISemesterService semesterService;

    /**
     * 查询开课
     * 
     * @param offeringId 开课主键
     * @return 开课
     */
    @Override
    public CourseOffering selectCourseOfferingByOfferingId(Long offeringId)
    {
        return courseOfferingMapper.selectCourseOfferingByOfferingId(offeringId);
    }

    /**
     * 查询开课列表
     * 
     * @param courseOffering 开课
     * @return 开课
     */
    @Override
    public List<CourseOffering> selectCourseOfferingList(CourseOffering courseOffering)
    {
        return courseOfferingMapper.selectCourseOfferingList(courseOffering);
    }

    /**
     * 新增开课
     * 
     * @param courseOffering 开课
     * @return 结果
     */
    @Override
    public int insertCourseOffering(CourseOffering courseOffering)
    {
        normalizeSchedule(courseOffering);
        checkTeacherScheduleConflict(courseOffering);
        checkLocationScheduleConflict(courseOffering);
        return courseOfferingMapper.insertCourseOffering(courseOffering);
    }

    /**
     * 修改开课
     * 
     * @param courseOffering 开课
     * @return 结果
     */
    @Override
    public int updateCourseOffering(CourseOffering courseOffering)
    {
        normalizeSchedule(courseOffering);
        checkCapacityNotBelowSelected(courseOffering);
        CourseOffering mergedOffering = mergeForValidation(courseOffering);
        checkTeacherScheduleConflict(mergedOffering);
        checkLocationScheduleConflict(mergedOffering);
        return courseOfferingMapper.updateCourseOffering(courseOffering);
    }

    /**
     * 批量删除开课
     * 
     * @param offeringIds 需要删除的开课主键
     * @return 结果
     */
    @Override
    public int deleteCourseOfferingByOfferingIds(Long[] offeringIds)
    {
        for (Long offeringId : offeringIds)
        {
            validateAndDeleteEnrollmentsBeforeOfferingDelete(offeringId);
        }
        return courseOfferingMapper.deleteCourseOfferingByOfferingIds(offeringIds);
    }

    /**
     * 删除开课信息
     * 
     * @param offeringId 开课主键
     * @return 结果
     */
    @Override
    public int deleteCourseOfferingByOfferingId(Long offeringId)
    {
        validateAndDeleteEnrollmentsBeforeOfferingDelete(offeringId);
        return courseOfferingMapper.deleteCourseOfferingByOfferingId(offeringId);
    }

    private void checkCapacityNotBelowSelected(CourseOffering courseOffering)
    {
        if (courseOffering == null || courseOffering.getOfferingId() == null || courseOffering.getMaxCapacity() == null)
        {
            return;
        }

        int selectedCount = enrollmentMapper.countActiveByOfferingId(courseOffering.getOfferingId());
        if (courseOffering.getMaxCapacity() < selectedCount)
        {
            throw new ServiceException("开课容量不能小于当前已选人数：" + selectedCount);
        }
    }

    private void normalizeSchedule(CourseOffering courseOffering)
    {
        if (courseOffering == null || isBlank(courseOffering.getSchedule()))
        {
            return;
        }
        courseOffering.setSchedule(formatScheduleAsTimeRange(courseOffering.getSchedule()));
    }

    private CourseOffering mergeForValidation(CourseOffering courseOffering)
    {
        if (courseOffering == null || courseOffering.getOfferingId() == null)
        {
            return courseOffering;
        }

        CourseOffering existingOffering = courseOfferingMapper.selectCourseOfferingByOfferingId(courseOffering.getOfferingId());
        if (existingOffering == null)
        {
            return courseOffering;
        }

        CourseOffering mergedOffering = new CourseOffering();
        mergedOffering.setOfferingId(courseOffering.getOfferingId());
        mergedOffering.setCourseId(courseOffering.getCourseId() != null ? courseOffering.getCourseId() : existingOffering.getCourseId());
        mergedOffering.setSemester(!isBlank(courseOffering.getSemester()) ? courseOffering.getSemester() : existingOffering.getSemester());
        mergedOffering.setTeacherId(courseOffering.getTeacherId() != null ? courseOffering.getTeacherId() : existingOffering.getTeacherId());
        mergedOffering.setSchedule(courseOffering.getSchedule() != null ? courseOffering.getSchedule() : existingOffering.getSchedule());
        mergedOffering.setLocation(courseOffering.getLocation() != null ? courseOffering.getLocation() : existingOffering.getLocation());
        mergedOffering.setMaxCapacity(courseOffering.getMaxCapacity() != null ? courseOffering.getMaxCapacity() : existingOffering.getMaxCapacity());
        return mergedOffering;
    }

    private void validateAndDeleteEnrollmentsBeforeOfferingDelete(Long offeringId)
    {
        CourseOffering offering = courseOfferingMapper.selectCourseOfferingByOfferingId(offeringId);
        if (offering == null)
        {
            return;
        }

        int selectedCount = enrollmentMapper.countActiveByOfferingId(offeringId);
        if (selectedCount <= 0)
        {
            return;
        }

        Semester currentSemester = semesterService.selectCurrentSemester();
        String currentSemesterValue = currentSemester == null ? null : currentSemester.getSemesterValue();
        if (offering.getSemester() == null || !offering.getSemester().equals(currentSemesterValue))
        {
            throw new ServiceException("历史学期已有学生选课，不能删除该开课记录");
        }

        enrollmentMapper.deleteByOfferingId(offeringId);
    }

    private void checkTeacherScheduleConflict(CourseOffering courseOffering)
    {
        if (courseOffering == null || courseOffering.getTeacherId() == null
                || isBlank(courseOffering.getSemester()) || isBlank(courseOffering.getSchedule()))
        {
            return;
        }

        List<TimeSlot> newSlots = parseSchedule(courseOffering.getSchedule());
        if (newSlots.isEmpty())
        {
            throw new ServiceException("上课时间格式无法识别，请选择有效的星期和时间段");
        }

        List<CourseOffering> existingOfferings = courseOfferingMapper.selectTeacherOfferingsForConflict(
                courseOffering.getSemester(), courseOffering.getTeacherId(), courseOffering.getOfferingId());
        for (CourseOffering existingOffering : existingOfferings)
        {
            List<TimeSlot> existingSlots = parseSchedule(existingOffering.getSchedule());
            for (TimeSlot newSlot : newSlots)
            {
                for (TimeSlot existingSlot : existingSlots)
                {
                    if (newSlot.conflictsWith(existingSlot))
                    {
                        throw new ServiceException("该教师在当前学期的该时间段已有开课，不能重复开课");
                    }
                }
            }
        }
    }

    private void checkLocationScheduleConflict(CourseOffering courseOffering)
    {
        if (courseOffering == null || isBlank(courseOffering.getSemester())
                || isBlank(courseOffering.getSchedule()) || isBlank(courseOffering.getLocation()))
        {
            return;
        }

        List<TimeSlot> newSlots = parseSchedule(courseOffering.getSchedule());
        if (newSlots.isEmpty())
        {
            throw new ServiceException("上课时间格式无法识别，请选择有效的星期和时间段");
        }

        List<CourseOffering> existingOfferings = courseOfferingMapper.selectLocationOfferingsForConflict(
                courseOffering.getSemester(), courseOffering.getLocation(), courseOffering.getOfferingId());
        for (CourseOffering existingOffering : existingOfferings)
        {
            List<TimeSlot> existingSlots = parseSchedule(existingOffering.getSchedule());
            for (TimeSlot newSlot : newSlots)
            {
                for (TimeSlot existingSlot : existingSlots)
                {
                    if (newSlot.conflictsWith(existingSlot))
                    {
                        throw new ServiceException("该上课地点在当前学期该时间段已被占用，不能重复开课");
                    }
                }
            }
        }
    }

    private List<TimeSlot> parseSchedule(String schedule)
    {
        List<TimeSlot> slots = new ArrayList<>();
        if (isBlank(schedule))
        {
            return slots;
        }

        String[] parts = schedule.split("[,，;；]");
        Pattern sectionPattern = Pattern.compile("(周[一二三四五六日])\\s*(\\d+)\\s*-\\s*(\\d+)\\s*节");
        Pattern timePattern = Pattern.compile("(周[一二三四五六日])\\s*([0-2]?\\d:[0-5]\\d)\\s*-\\s*([0-2]?\\d:[0-5]\\d)");

        for (String part : parts)
        {
            String item = part.trim();
            Matcher sectionMatcher = sectionPattern.matcher(item);
            if (sectionMatcher.matches())
            {
                int startSection = Integer.parseInt(sectionMatcher.group(2));
                int endSection = Integer.parseInt(sectionMatcher.group(3));
                slots.add(new TimeSlot(convertDayToNumber(sectionMatcher.group(1)), startSection, endSection));
                continue;
            }

            Matcher timeMatcher = timePattern.matcher(item);
            if (timeMatcher.matches())
            {
                int startMinute = convertTimeToMinutes(timeMatcher.group(2));
                int endMinute = convertTimeToMinutes(timeMatcher.group(3));
                int[] sections = convertMinutesToSections(startMinute, endMinute);
                if (sections[0] > 0 && sections[1] > 0)
                {
                    slots.add(new TimeSlot(convertDayToNumber(timeMatcher.group(1)), sections[0], sections[1]));
                }
            }
        }
        return slots;
    }

    private String formatScheduleAsTimeRange(String schedule)
    {
        String[] parts = schedule.split("[,，;；]");
        List<String> normalizedParts = new ArrayList<>();
        Pattern sectionPattern = Pattern.compile("(周[一二三四五六日])\\s*(\\d+)\\s*-\\s*(\\d+)\\s*节");

        for (String part : parts)
        {
            String item = part.trim();
            Matcher sectionMatcher = sectionPattern.matcher(item);
            if (sectionMatcher.matches())
            {
                String timeRange = convertSectionsToTimeRange(
                        Integer.parseInt(sectionMatcher.group(2)),
                        Integer.parseInt(sectionMatcher.group(3)));
                if (timeRange != null)
                {
                    normalizedParts.add(sectionMatcher.group(1) + " " + timeRange);
                    continue;
                }
            }
            normalizedParts.add(item);
        }
        return String.join(",", normalizedParts);
    }

    private String convertSectionsToTimeRange(int startSection, int endSection)
    {
        int start = Math.min(startSection, endSection);
        int end = Math.max(startSection, endSection);
        if (start == 1 && end == 2)
        {
            return "8:00-9:40";
        }
        if (start == 3 && end == 4)
        {
            return "10:00-11:40";
        }
        if (start == 5 && end == 6)
        {
            return "14:00-15:40";
        }
        if (start == 7 && end == 8)
        {
            return "16:00-17:40";
        }
        if (start == 9 && end == 10)
        {
            return "19:00-20:40";
        }
        return null;
    }

    private int convertDayToNumber(String day)
    {
        switch (day)
        {
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

    private int convertTimeToMinutes(String time)
    {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private int[] convertMinutesToSections(int startMinute, int endMinute)
    {
        return new int[] { convertMinuteToSection(startMinute), convertMinuteToSection(endMinute) };
    }

    private int convertMinuteToSection(int minute)
    {
        if (minute < 10 * 60)
        {
            return minute < 9 * 60 ? 1 : 2;
        }
        if (minute < 12 * 60)
        {
            return minute < 11 * 60 ? 3 : 4;
        }
        if (minute < 16 * 60)
        {
            return minute < 15 * 60 ? 5 : 6;
        }
        if (minute < 18 * 60)
        {
            return minute < 17 * 60 ? 7 : 8;
        }
        if (minute < 21 * 60)
        {
            return minute < 20 * 60 ? 9 : 10;
        }
        return 0;
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private static class TimeSlot
    {
        private final int day;
        private final int startSection;
        private final int endSection;

        private TimeSlot(int day, int startSection, int endSection)
        {
            this.day = day;
            this.startSection = Math.min(startSection, endSection);
            this.endSection = Math.max(startSection, endSection);
        }

        private boolean conflictsWith(TimeSlot other)
        {
            return day == other.day && startSection <= other.endSection && endSection >= other.startSection;
        }
    }
}
