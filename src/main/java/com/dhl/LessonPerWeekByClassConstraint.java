package com.dhl;

public class LessonPerWeekByClassConstraint {

    private Long clazzId;

    private String subjectId;

    private Integer lessonPerWeek;

    public Long getClazzId() {
        return clazzId;
    }

    public void setClazzId(Long clazzId) {
        this.clazzId = clazzId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getLessonPerWeek() {
        return lessonPerWeek;
    }

    public void setLessonPerWeek(Integer lessonPerWeek) {
        this.lessonPerWeek = lessonPerWeek;
    }
}
