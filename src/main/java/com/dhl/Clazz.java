package com.dhl;

public class Clazz {

    private Long id;

    private String name;

    private Integer grade;

    private Teacher teacher;

    public Clazz() { }

    public Clazz(Long id, String name, Integer grade, Teacher teacher) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.teacher = teacher;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }
}
