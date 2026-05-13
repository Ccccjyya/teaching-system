package com.ruoyi.edu.vo;

public class GradeStatisticsVO {
    private Integer totalCredit;
    private Double avgGpa;
    private Integer excellentCount;
    private Integer goodCount;
    private Integer mediumCount;
    private Integer passCount;
    private Integer failCount;
    private Integer totalCourses;

    public Integer getTotalCredit() { return totalCredit; }
    public void setTotalCredit(Integer totalCredit) { this.totalCredit = totalCredit; }
    public Double getAvgGpa() { return avgGpa; }
    public void setAvgGpa(Double avgGpa) { this.avgGpa = avgGpa; }
    public Integer getExcellentCount() { return excellentCount; }
    public void setExcellentCount(Integer excellentCount) { this.excellentCount = excellentCount; }
    public Integer getGoodCount() { return goodCount; }
    public void setGoodCount(Integer goodCount) { this.goodCount = goodCount; }
    public Integer getMediumCount() { return mediumCount; }
    public void setMediumCount(Integer mediumCount) { this.mediumCount = mediumCount; }
    public Integer getPassCount() { return passCount; }
    public void setPassCount(Integer passCount) { this.passCount = passCount; }
    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }
    public Integer getTotalCourses() { return totalCourses; }
    public void setTotalCourses(Integer totalCourses) { this.totalCourses = totalCourses; }
}