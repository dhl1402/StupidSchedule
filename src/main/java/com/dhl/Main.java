package com.dhl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.util.List;
public class Main {

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File subjectFile = new File(new URI(Main.class.getResource("/data/subjects.json").toString()));
            List<Subject> subjects = mapper.readValue(subjectFile, mapper.getTypeFactory().constructCollectionType(List.class, Subject.class));

            File clazzFile = new File(new URI(Main.class.getResource("/data/clazzes.json").toString()));
            List<Clazz> clazzes = mapper.readValue(clazzFile, mapper.getTypeFactory().constructCollectionType(List.class, Clazz.class));

            File teacherFile = new File(new URI(Main.class.getResource("/data/teachers.json").toString()));
            List<Teacher> teachers = mapper.readValue(teacherFile, mapper.getTypeFactory().constructCollectionType(List.class, Teacher.class));

            File lessonTimeFile = new File(new URI(Main.class.getResource("/data/lessonTimes.json").toString()));
            List<LessonTime> lessonTimes = mapper.readValue(lessonTimeFile, mapper.getTypeFactory().constructCollectionType(List.class, LessonTime.class));

            File cf1 = new File(new URI(Main.class.getResource("/data/lessonTimeOffConstraintByClazz.json").toString()));
            List<LessonTimeOffByClazzConstraint> lessonTimeOffByClazzConstraints = mapper.readValue(cf1, mapper.getTypeFactory().constructCollectionType(List.class, LessonTimeOffByClazzConstraint.class));

            File cf2 = new File(new URI(Main.class.getResource("/data/lessonPerWeekConstraintByClazz.json").toString()));
            List<LessonPerWeekByClassConstraint> lessonPerWeekByClassConstraints = mapper.readValue(cf2, mapper.getTypeFactory().constructCollectionType(List.class, LessonPerWeekByClassConstraint.class));

            long start = System.nanoTime();
            Scheduler scheduler = new Scheduler(lessonTimes, clazzes, teachers, subjects, lessonTimeOffByClazzConstraints, lessonPerWeekByClassConstraints);
            if(scheduler.canScheduleWithNoConflict()) {
                Schedule schedule = scheduler.startScheduling();
                scheduler.exportSchedule(schedule);
            } else {
                System.out.println("Constraint conflicts found");
            }
            long end = System.nanoTime();
            System.out.println("Done after: " + (end-start)/1000000 + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
