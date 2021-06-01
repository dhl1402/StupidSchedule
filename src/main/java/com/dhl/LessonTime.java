package com.dhl;

import java.util.Objects;

public class LessonTime {

    private Integer dayOfWeek;

    private Integer lessonIndex;

    public LessonTime() { }

    public LessonTime(Integer dayOfWeek, Integer lessonIndex) {
        this.dayOfWeek = dayOfWeek;
        this.lessonIndex = lessonIndex;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonTime that = (LessonTime) o;
        return Objects.equals(dayOfWeek, that.dayOfWeek) && Objects.equals(lessonIndex, that.lessonIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, lessonIndex);
    }
}
