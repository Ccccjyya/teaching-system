package com.ruoyi.edu.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.util.CollectionUtils;
import com.ruoyi.edu.mapper.EnrollmentMapper;
import com.ruoyi.edu.mapper.CourseOfferingMapper;
import com.ruoyi.edu.domain.Department;
import com.ruoyi.edu.domain.Enrollment;
import com.ruoyi.edu.domain.CourseOffering;
import com.ruoyi.edu.domain.Student;
import com.ruoyi.edu.service.IEnrollmentService;
import com.ruoyi.edu.service.IStudentService;
import com.ruoyi.edu.vo.CourseTableVO;
import com.ruoyi.edu.vo.GradeStatisticsVO;
import com.ruoyi.edu.vo.StudentGradeVO;
import com.ruoyi.system.service.ISysUserService;

/**
 * 选课Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-05-11
 */
@Service
public class EnrollmentServiceImpl implements IEnrollmentService 
{
    private static final Logger log = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private CourseOfferingMapper courseOfferingMapper;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 查询选课
     * 
     * @param enrollmentId 选课主键
     * @return 选课
     */
    @Override
    public Enrollment selectEnrollmentByEnrollmentId(Long enrollmentId)
    {
        return enrollmentMapper.selectEnrollmentByEnrollmentId(enrollmentId);
    }

    /**
     * 查询选课列表
     * 
     * @param enrollment 选课
     * @return 选课
     */
    @Override
    public List<Enrollment> selectEnrollmentList(Enrollment enrollment)
    {
        return enrollmentMapper.selectEnrollmentList(enrollment);
    }

    /**
     * 查询学生已选课程
     * 
     * @param studentId 学生ID
     * @return 选课集合
     */
    @Override
    public List<Enrollment> selectEnrollmentByStudentId(Long studentId)
    {
        return enrollmentMapper.selectEnrollmentByStudentId(studentId);
    }

    /**
     * 新增选课
     * 
     * @param enrollment 选课
     * @return 结果
     */
    @Override
    public int insertEnrollment(Enrollment enrollment)
    {
        fillEnrollmentFromOffering(enrollment);
        return enrollmentMapper.insertEnrollment(enrollment);
    }

    /**
     * 学生选课（带时间冲突检测）
     * 
     * @param enrollment 选课
     * @return 结果
     */
    @Override
    @Transactional
    public int studentEnroll(Enrollment enrollment)
    {
        // 1. 参数校验
        if (StringUtils.isNull(enrollment.getOfferingId()) || enrollment.getOfferingId() <= 0)
        {
            throw new ServiceException("开课ID无效");
        }
        if (StringUtils.isNull(enrollment.getStudentId()) || enrollment.getStudentId() <= 0)
        {
            throw new ServiceException("学生ID无效");
        }
        
        // 2. 查询要选的开课信息
        CourseOffering courseOffering = courseOfferingMapper.selectCourseOfferingByOfferingId(enrollment.getOfferingId());
        if (StringUtils.isNull(courseOffering))
        {
            throw new ServiceException("开课信息不存在");
        }

        Enrollment existingEnrollment = enrollmentMapper.selectActiveEnrollmentByStudentIdAndOfferingId(
                enrollment.getStudentId(), enrollment.getOfferingId());
        if (existingEnrollment != null)
        {
            throw new ServiceException("您已选择该课程，无需重复选课");
        }

        // 3. 检查课程容量
        if (courseOffering.getSelectedCount() >= courseOffering.getMaxCapacity())
        {
            throw new ServiceException("课程已选满，无法继续选课");
        }

        // 4. 检查时间冲突
        checkTimeConflict(enrollment.getStudentId(), courseOffering);

        // 5. 补全选课信息
        enrollment.setCourseNo(courseOffering.getCourseNo());
        enrollment.setTeacherId(courseOffering.getTeacherId());
        enrollment.setStatus("0");
        
        // 从开课中获取学期信息并解析
        if (courseOffering.getSemester() != null) {
            String courseSemester = courseOffering.getSemester();
            // 格式如 "2024-2025-1"，拆分为 academic_year 和 semester
            String[] parts = courseSemester.split("-");
            if (parts.length >= 3) {
                // "2024-2025-1" -> academic_year = "2024-2025", semester = "1"
                enrollment.setAcademicYear(parts[0] + "-" + parts[1]);
                enrollment.setSemester(parts[2]);
            }
        }

        // 6. 保存选课记录（重复选课用唯一键兜底，避免误判）
        int result;
        try
        {
            result = enrollmentMapper.insertEnrollment(enrollment);
        }
        catch (DuplicateKeyException ex)
        {
            // 这里出现说明 (student_id, offering_id) 已存在；通常是前端传了同一个 offeringId
            log.warn("Duplicate enrollment: studentId={}, offeringId={}", enrollment.getStudentId(), enrollment.getOfferingId());
            // 保持原提示文案，追加关键信息方便排查 offeringId 是否一直不变
            throw new ServiceException("您已选择该课程，无需重复选课（开课ID=" + enrollment.getOfferingId() + "，学生ID=" + enrollment.getStudentId() + "）");
        }

        return result;
    }

    /**
     * 检查时间冲突
     * 
     * @param studentId 学生ID
     * @param newCourseOffering 要选的课程
     */
    private void checkTimeConflict(Long studentId, CourseOffering newCourseOffering)
    {
        // 查询学生已选的有效课程
        List<Enrollment> enrollmentList = enrollmentMapper.selectEnrollmentByStudentId(studentId);
        
        // 获取新课程的时间
        String newSchedule = newCourseOffering.getSchedule();
        if (StringUtils.isEmpty(newSchedule))
        {
            return;
        }

        // 解析新课程时间
        List<TimeSlot> newTimeSlots = parseSchedule(newSchedule);
        if (newTimeSlots.isEmpty())
        {
            throw new ServiceException("选课失败：课程时间格式无法识别，请联系管理员检查开课时间");
        }

        // 检查每一门已选课程是否有时间冲突
        for (Enrollment enrollment : enrollmentList)
        {
            CourseOffering existingOffering = enrollment.getCourseOffering();
            if (StringUtils.isNull(existingOffering))
            {
                continue;
            }
            
            String existingSchedule = existingOffering.getSchedule();
            if (StringUtils.isEmpty(existingSchedule))
            {
                continue;
            }

            // 解析已选课程时间
            List<TimeSlot> existingTimeSlots = parseSchedule(existingSchedule);
            if (existingTimeSlots.isEmpty())
            {
                throw new ServiceException("选课失败：已选课程时间格式无法识别，请联系管理员检查开课时间");
            }

            // 检查时间冲突
            for (TimeSlot newSlot : newTimeSlots)
            {
                for (TimeSlot existingSlot : existingTimeSlots)
                {
                    if (hasConflict(newSlot, existingSlot))
                    {
                        throw new ServiceException("选课失败：课程 " + existingOffering.getCourse().getCourseName() + 
                            " 与课程 " + newCourseOffering.getCourse().getCourseName() + 
                            " 时间冲突");
                    }
                }
            }
        }
    }

    /**
     * 解析上课时间字符串，兼容旧格式“周一5-6节”和新格式“周一 14:00-15:40”
     */
    private List<TimeSlot> parseSchedule(String schedule)
    {
        List<TimeSlot> timeSlots = new java.util.ArrayList<>();
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
                
                TimeSlot slot = new TimeSlot();
                slot.setDay(convertDayToNumber(sectionMatcher.group(1)));
                slot.setStartSection(startSection);
                slot.setEndSection(endSection);
                int[] minuteRange = convertSectionToMinutes(startSection, endSection);
                slot.setStartMinute(minuteRange[0]);
                slot.setEndMinute(minuteRange[1]);
                timeSlots.add(slot);
                continue;
            }

            Matcher timeMatcher = timePattern.matcher(item);
            if (timeMatcher.matches())
            {
                int startMinute = convertTimeToMinutes(timeMatcher.group(2));
                int endMinute = convertTimeToMinutes(timeMatcher.group(3));
                if (startMinute >= 0 && endMinute >= startMinute)
                {
                    TimeSlot slot = new TimeSlot();
                    slot.setDay(convertDayToNumber(timeMatcher.group(1)));
                    slot.setStartMinute(startMinute);
                    slot.setEndMinute(endMinute);
                    int[] sectionRange = convertMinutesToSection(startMinute, endMinute);
                    slot.setStartSection(sectionRange[0]);
                    slot.setEndSection(sectionRange[1]);
                    timeSlots.add(slot);
                }
            }
        }
        return timeSlots;
    }

    private int convertTimeToMinutes(String time)
    {
        if (StringUtils.isEmpty(time))
        {
            return -1;
        }
        String[] parts = time.split(":");
        if (parts.length != 2)
        {
            return -1;
        }
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private int[] convertSectionToMinutes(int startSection, int endSection)
    {
        return new int[] { getSectionStartMinute(startSection), getSectionEndMinute(endSection) };
    }

    private int[] convertMinutesToSection(int startMinute, int endMinute)
    {
        int startSection = 0;
        int endSection = 0;
        for (int section = 1; section <= 10; section++)
        {
            if (startSection == 0 && getSectionStartMinute(section) == startMinute)
            {
                startSection = section;
            }
            if (getSectionEndMinute(section) == endMinute)
            {
                endSection = section;
            }
        }
        return new int[] { startSection, endSection };
    }

    private int getSectionStartMinute(int section)
    {
        switch (section)
        {
            case 1: return 8 * 60;
            case 2: return 8 * 60 + 55;
            case 3: return 10 * 60;
            case 4: return 10 * 60 + 55;
            case 5: return 14 * 60;
            case 6: return 14 * 60 + 55;
            case 7: return 16 * 60;
            case 8: return 16 * 60 + 55;
            case 9: return 19 * 60;
            case 10: return 19 * 60 + 55;
            default: return -1;
        }
    }

    private int getSectionEndMinute(int section)
    {
        switch (section)
        {
            case 1: return 8 * 60 + 45;
            case 2: return 9 * 60 + 40;
            case 3: return 10 * 60 + 45;
            case 4: return 11 * 60 + 40;
            case 5: return 14 * 60 + 45;
            case 6: return 15 * 60 + 40;
            case 7: return 16 * 60 + 45;
            case 8: return 17 * 60 + 40;
            case 9: return 19 * 60 + 45;
            case 10: return 20 * 60 + 40;
            default: return -1;
        }
    }

    /**
     * 将星期几转换为数字
     */
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

    /**
     * 检查两个时间段是否冲突
     */
    private boolean hasConflict(TimeSlot slot1, TimeSlot slot2)
    {
        // 不同天不冲突
        if (slot1.getDay() != slot2.getDay())
        {
            return false;
        }
        
        if (slot1.getStartMinute() >= 0 && slot1.getEndMinute() >= 0
                && slot2.getStartMinute() >= 0 && slot2.getEndMinute() >= 0)
        {
            return !(slot1.getEndMinute() <= slot2.getStartMinute()
                    || slot2.getEndMinute() <= slot1.getStartMinute());
        }

        return !(slot1.getEndSection() < slot2.getStartSection()
                || slot2.getEndSection() < slot1.getStartSection());
    }

    /**
     * 修改选课
     * 
     * @param enrollment 选课
     * @return 结果
     */
    @Override
    public int updateEnrollment(Enrollment enrollment)
    {
        fillEnrollmentFromOffering(enrollment);
        return enrollmentMapper.updateEnrollment(enrollment);
    }

    private void fillEnrollmentFromOffering(Enrollment enrollment)
    {
        if (enrollment == null || enrollment.getOfferingId() == null)
        {
            return;
        }
        CourseOffering courseOffering = courseOfferingMapper.selectCourseOfferingByOfferingId(enrollment.getOfferingId());
        if (StringUtils.isNull(courseOffering))
        {
            throw new ServiceException("开课信息不存在");
        }
        enrollment.setCourseNo(courseOffering.getCourseNo());
        enrollment.setTeacherId(courseOffering.getTeacherId());
        String courseSemester = courseOffering.getSemester();
        if (StringUtils.isNotEmpty(courseSemester))
        {
            String[] parts = courseSemester.split("-");
            if (parts.length >= 3)
            {
                enrollment.setAcademicYear(parts[0] + "-" + parts[1]);
                enrollment.setSemester(parts[2]);
            }
        }
    }

    /**
     * 批量删除选课
     * 
     * @param enrollmentIds 需要删除的选课主键
     * @return 结果
     */
    @Override
    public int deleteEnrollmentByEnrollmentIds(Long[] enrollmentIds)
    {
        return enrollmentMapper.deleteEnrollmentByEnrollmentIds(enrollmentIds);
    }

    /**
     * 删除选课信息（退课）
     * 
     * @param enrollmentId 选课主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteEnrollmentByEnrollmentId(Long enrollmentId)
    {
        // 1. 查询选课信息
        Enrollment enrollment = enrollmentMapper.selectEnrollmentByEnrollmentId(enrollmentId);
        if (StringUtils.isNull(enrollment))
        {
            throw new ServiceException("选课记录不存在");
        }

        // 2. 删除选课记录
        int result = enrollmentMapper.deleteEnrollmentByEnrollmentId(enrollmentId);

        return result;
    }

    /**
     * 时间段内部类
     */
    private static class TimeSlot
    {
        private int day;
        private int startSection;
        private int endSection;
        private int startMinute = -1;
        private int endMinute = -1;

        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
        public int getStartSection() { return startSection; }
        public void setStartSection(int startSection) { this.startSection = startSection; }
        public int getEndSection() { return endSection; }
        public void setEndSection(int endSection) { this.endSection = endSection; }
        public int getStartMinute() { return startMinute; }
        public void setStartMinute(int startMinute) { this.startMinute = startMinute; }
        public int getEndMinute() { return endMinute; }
        public void setEndMinute(int endMinute) { this.endMinute = endMinute; }
    }

    @Override
    public List<StudentGradeVO> selectStudentGrades(Long studentId, String academicYear, String semester) {
        List<StudentGradeVO> grades = enrollmentMapper.selectStudentGrades(studentId, academicYear, semester);
        for (StudentGradeVO grade : grades) {
            if (grade.getTotalScore() != null) {
                grade.setGpa(calculateGpa(grade.getTotalScore()));
            }
        }
        return grades;
    }

    @Override
    public GradeStatisticsVO calculateGradeStatistics(Long studentId, String academicYear, String semester) {
        List<StudentGradeVO> grades = enrollmentMapper.selectStudentGrades(studentId, academicYear, semester);
        
        GradeStatisticsVO statistics = new GradeStatisticsVO();
        int totalCredit = 0;
        double totalGpaPoints = 0;
        int excellentCount = 0;
        int goodCount = 0;
        int mediumCount = 0;
        int passCount = 0;
        int failCount = 0;
        
        for (StudentGradeVO grade : grades) {
            Double totalScore = grade.getTotalScore();
            Integer credit = grade.getCredit();
            
            if (totalScore != null && credit != null && credit > 0 && totalScore >= 60) {
                totalCredit += credit;
                
                double gpa = calculateGpa(totalScore);
                grade.setGpa(gpa);
                totalGpaPoints += gpa * credit;
                
                if (totalScore >= 90) {
                    excellentCount++;
                } else if (totalScore >= 80) {
                    goodCount++;
                } else if (totalScore >= 70) {
                    mediumCount++;
                } else if (totalScore >= 60) {
                    passCount++;
                }
            } else if (totalScore != null && totalScore < 60) {
                failCount++;
            }
        }
        
        statistics.setTotalCredit(totalCredit);
        statistics.setTotalCourses(grades.size());
        statistics.setExcellentCount(excellentCount);
        statistics.setGoodCount(goodCount);
        statistics.setMediumCount(mediumCount);
        statistics.setPassCount(passCount);
        statistics.setFailCount(failCount);
        
        if (totalCredit > 0) {
            double avgGpa = totalGpaPoints / totalCredit;
            statistics.setAvgGpa(Math.round(avgGpa * 100.0) / 100.0);
        } else {
            statistics.setAvgGpa(0.0);
        }
        
        return statistics;
    }

    @Override
    public List<CourseTableVO> selectStudentCourseTable(Long studentId, String academicYear, String semester) {
        List<CourseTableVO> courseTable = enrollmentMapper.selectStudentCourseTable(studentId, academicYear, semester);
        
        Map<String, String> courseColors = new HashMap<>();
        String[] colors = {"#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de", 
                           "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc"};
        int colorIndex = 0;
        
        for (CourseTableVO course : courseTable) {
            String courseKey = course.getCourseNo();
            if (!courseColors.containsKey(courseKey)) {
                courseColors.put(courseKey, colors[colorIndex % colors.length]);
                colorIndex++;
            }
            course.setColor(courseColors.get(courseKey));
            
            parseScheduleForCourseTable(course);
        }
        
        return courseTable;
    }

    @Override
    public List<String> selectStudentEnrollmentSemesters(Long studentId) {
        return enrollmentMapper.selectStudentEnrollmentSemesters(studentId);
    }

    private double calculateGpa(Double score) {
        if (score >= 90) {
            return 4.0;
        } else if (score >= 85) {
            return 3.7;
        } else if (score >= 82) {
            return 3.3;
        } else if (score >= 78) {
            return 3.0;
        } else if (score >= 75) {
            return 2.7;
        } else if (score >= 72) {
            return 2.3;
        } else if (score >= 68) {
            return 2.0;
        } else if (score >= 64) {
            return 1.5;
        } else if (score >= 60) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    private void parseScheduleForCourseTable(CourseTableVO course) {
        String schedule = course.getSchedule();
        if (schedule == null || schedule.isEmpty()) {
            return;
        }
        
        String[] parts = schedule.split(",");
        if (parts.length > 0) {
            String firstPart = parts[0].trim();
            
            String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            for (int i = 1; i <= 7; i++) {
                if (firstPart.startsWith(weekDays[i])) {
                    course.setDayOfWeek(i);
                    break;
                }
            }
            
            Pattern periodPattern = Pattern.compile("(\\d+)-(\\d+)节");
            Matcher periodMatcher = periodPattern.matcher(firstPart);
            if (periodMatcher.find()) {
                course.setStartPeriod(Integer.parseInt(periodMatcher.group(1)));
                course.setEndPeriod(Integer.parseInt(periodMatcher.group(2)));
                return;
            }

            Pattern timePattern = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*-\\s*(\\d{1,2}):(\\d{2})");
            Matcher timeMatcher = timePattern.matcher(firstPart);
            if (timeMatcher.find()) {
                int startHour = Integer.parseInt(timeMatcher.group(1));
                int startMinute = Integer.parseInt(timeMatcher.group(2));
                int endHour = Integer.parseInt(timeMatcher.group(3));
                int endMinute = Integer.parseInt(timeMatcher.group(4));
                int startPeriod = convertTimeToPeriod(startHour, startMinute);
                int endPeriod = convertTimeToPeriod(endHour, endMinute);
                if (startPeriod > 0 && endPeriod > 0) {
                    course.setStartPeriod(startPeriod);
                    course.setEndPeriod(Math.max(startPeriod, endPeriod));
                }
            }
        }
    }

    private int convertTimeToPeriod(int hour, int minute) {
        int totalMinutes = hour * 60 + minute;
        if (totalMinutes < 10 * 60) {
            return totalMinutes < 9 * 60 ? 1 : 2;
        } else if (totalMinutes < 12 * 60) {
            return totalMinutes < 11 * 60 ? 3 : 4;
        } else if (totalMinutes < 16 * 60) {
            return totalMinutes < 15 * 60 ? 5 : 6;
        } else if (totalMinutes < 18 * 60) {
            return totalMinutes < 17 * 60 ? 7 : 8;
        } else if (totalMinutes < 21 * 60) {
            return totalMinutes < 20 * 60 ? 9 : 10;
        }
        return 0;
    }

    @Override
    public Map<String, Object> getStudentStats(Long studentId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 查询当前学期已选课程数
        List<Enrollment> currentEnrollments = enrollmentMapper.selectCurrentSemesterEnrollments(studentId);
        int courseCount = currentEnrollments != null ? currentEnrollments.size() : 0;
        stats.put("courseCount", courseCount);
        
        // 查询已修学分（成绩 >= 60 的课程学分总和）
        int creditCount = enrollmentMapper.sumCompletedCredits(studentId);
        stats.put("creditCount", creditCount);

        // 查询总GPA（按学分加权）
        List<StudentGradeVO> grades = selectStudentGrades(studentId, null, null);
        double totalGpaPoints = 0D;
        int totalCredits = 0;
        if (!CollectionUtils.isEmpty(grades))
        {
            for (StudentGradeVO grade : grades)
            {
                if (grade == null || grade.getGpa() == null || grade.getCredit() == null || grade.getCredit() <= 0)
                {
                    continue;
                }
                totalGpaPoints += grade.getGpa() * grade.getCredit();
                totalCredits += grade.getCredit();
            }
        }
        double totalGpa = totalCredits > 0 ? totalGpaPoints / totalCredits : 0D;
        stats.put("totalGpa", BigDecimal.valueOf(totalGpa).setScale(2, RoundingMode.HALF_UP).doubleValue());
        
        return stats;
    }

    @Override
    public List<Enrollment> getCourseStudents(Long offeringId) {
        return enrichStudentDetails(enrollmentMapper.selectCourseStudents(offeringId));
    }

    @Override
    public List<Enrollment> getCourseStudents(Long offeringId, String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return getCourseStudents(offeringId);
        }
        return enrichStudentDetails(enrollmentMapper.selectCourseStudentsByKeyword(offeringId, keyword));
    }

    private List<Enrollment> enrichStudentDetails(List<Enrollment> enrollments)
    {
        if (CollectionUtils.isEmpty(enrollments))
        {
            return enrollments;
        }
        for (Enrollment enrollment : enrollments)
        {
            if (enrollment == null || enrollment.getStudentId() == null)
            {
                continue;
            }
            Student fullStudent = studentService.selectStudentByStudentId(enrollment.getStudentId());
            if (fullStudent != null)
            {
                fillStudentExtraInfo(fullStudent);
                enrollment.setStudent(fullStudent);
            }
        }
        return enrollments;
    }

    private void fillStudentExtraInfo(Student student)
    {
        if (student == null || student.getUserId() == null)
        {
            return;
        }
        SysUser sysUser = sysUserService.selectUserById(student.getUserId());
        if (sysUser == null)
        {
            return;
        }
        if (StringUtils.isEmpty(student.getPhone()) && StringUtils.isNotEmpty(sysUser.getPhonenumber()))
        {
            student.setPhone(sysUser.getPhonenumber());
        }
        if ((student.getDept() == null || StringUtils.isEmpty(student.getDept().getDeptName())) && sysUser.getDept() != null)
        {
            SysDept sysDept = sysUser.getDept();
            Department department = student.getDept() != null ? student.getDept() : new Department();
            department.setDeptId(sysDept.getDeptId());
            department.setDeptName(sysDept.getDeptName());
            student.setDept(department);
        }
    }

    @Override
    @Transactional
    public int saveSingleScore(Long enrollmentId, Integer usualScore, Integer examScore, Integer totalScore) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        if (usualScore != null) {
            enrollment.setUsualScore(new java.math.BigDecimal(usualScore));
        }
        if (examScore != null) {
            enrollment.setExamScore(new java.math.BigDecimal(examScore));
        }
        if (totalScore != null) {
            enrollment.setTotalScore(new java.math.BigDecimal(totalScore));
        }
        return enrollmentMapper.updateEnrollment(enrollment);
    }

    @Override
    @Transactional
    public int saveScores(List<Map<String, Object>> scoreList) {
        int count = 0;
        for (Map<String, Object> score : scoreList) {
            Object enrollmentIdObj = score.get("enrollmentId");
            if (enrollmentIdObj != null) {
                Long enrollmentId = Long.parseLong(enrollmentIdObj.toString());
                Integer usualScore = null;
                if (score.get("usualScore") != null) {
                    usualScore = Integer.parseInt(score.get("usualScore").toString());
                }
                Integer examScore = null;
                if (score.get("examScore") != null) {
                    examScore = Integer.parseInt(score.get("examScore").toString());
                }
                Integer totalScore = null;
                if (score.get("totalScore") != null) {
                    totalScore = Integer.parseInt(score.get("totalScore").toString());
                }
                
                saveSingleScore(enrollmentId, usualScore, examScore, totalScore);
                count++;
            }
        }
        return count;
    }
}
