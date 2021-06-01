package com.dhl;

public class LessonTimeOffByClazzConstraint {

    private Long clazzId;

    private Integer dayOfWeek;

    private Integer lessonIndex;

    public Long getClazzId() {
        return clazzId;
    }

    public void setClazzId(Long clazzId) {
        this.clazzId = clazzId;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getLessonIndex() {
        return lessonIndex;
    }

    public void setLessonIndex(Integer lessonIndex) {
        this.lessonIndex = lessonIndex;
    }
}
