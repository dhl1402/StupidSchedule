package com.dhl;

import java.util.List;

public class Schedule {

    private List<Lesson> lessons;

    private Float fitness;

    public Schedule(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public static class Lesson {

        private LessonTime lessonTime;

        private Teacher teacher;

        private Subject subject;

        private Clazz clazz;

        public LessonTime getLessonTime() {
            return lessonTime;
        }

        public void setLessonTime(LessonTime lessonTime) {
            this.lessonTime = lessonTime;
        }

        public Teacher getTeacher() {
            return teacher;
        }

        public void setTeacher(Teacher teacher) {
            this.teacher = teacher;
        }

        public Subject getSubject() {
            return subject;
        }

        public void setSubject(Subject subject) {
            this.subject = subject;
        }

        public Clazz getClazz() {
            return clazz;
        }

        public void setClazz(Clazz clazz) {
            this.clazz = clazz;
        }

        public Boolean isPreviousLessonOf(Lesson lesson) {
            boolean isSameDay = getLessonTime().getDayOfWeek().equals(lesson.getLessonTime().getDayOfWeek());
            boolean isSameShift = Math.abs(getLessonTime().getLessonIndex() - lesson.getLessonTime().getLessonIndex()) < 5; // hardcoded 5 lessons per shift for now
            return isSameDay && isSameShift && getLessonTime().getLessonIndex() + 1 == lesson.getLessonTime().getLessonIndex();
        }
    }
}
