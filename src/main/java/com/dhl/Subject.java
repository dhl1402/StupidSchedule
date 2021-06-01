package com.dhl;

import java.util.List;
import java.util.Objects;

public class Subject {

    private String id;

    private String name;

    private Integer grade;

    private Integer lessonPerWeek;

    private List<Integer> lessonDistribution;

    public Subject() { }

    public Subject(String id, String name, Integer grade, Integer lessonPerWeek) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.lessonPerWeek = lessonPerWeek;
    }

    public Subject(String id, String name, Integer grade, Integer lessonPerWeek, List<Integer> lessonDistribution) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.lessonPerWeek = lessonPerWeek;
        this.lessonDistribution = lessonDistribution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLessonPerWeek() {
        return lessonPerWeek;
    }

    public void setLessonPerWeek(Integer lessonPerWeek) {
        this.lessonPerWeek = lessonPerWeek;
    }

    public List<Integer> getLessonDistribution() {
        return lessonDistribution;
    }

    public void setLessonDistribution(List<Integer> lessonDistribution) {
        this.lessonDistribution = lessonDistribution;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id.equals(subject.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
