package com.dhl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javatuples.Pair;

import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Scheduler {

    private final List<LessonTime> lessonTimes;

    private final List<Clazz> clazzes;

    private final List<Teacher> teachers;

    private final List<Subject> subjects;

    private final Map<Integer, List<Subject>> subjectsGroupedByGrade;

    private final List<LessonTimeOffByClazzConstraint> lessonTimeOffByClazzConstraints;

    private final List<LessonPerWeekByClassConstraint> lessonPerWeekByClassConstraints;

    private final Random rand = new Random();

    public Scheduler(
            List<LessonTime> lessonTimes,
            List<Clazz> clazzes,
            List<Teacher> teachers,
            List<Subject> subjects,
            List<LessonTimeOffByClazzConstraint> lessonTimeOffByClazzConstraints,
            List<LessonPerWeekByClassConstraint> lessonPerWeekByClassConstraints
    ) {
        this.lessonTimes = lessonTimes;
        this.clazzes = clazzes;
        this.teachers = teachers;
        this.subjects = subjects;
        this.lessonTimeOffByClazzConstraints = lessonTimeOffByClazzConstraints;
        this.lessonPerWeekByClassConstraints = lessonPerWeekByClassConstraints;
        this.subjectsGroupedByGrade = subjects.stream().collect(Collectors.groupingBy(Subject::getGrade));
    }

    public boolean canScheduleWithNoConflict() throws Exception {
        int constraintConflicts = 0;
        for (Clazz clazz : clazzes) {
            int expectedLessonCount = 0;
            int scheduledLessonCount = 0;
            for(LessonTime lessonTime : lessonTimes) {
                if (!isLessonOff(clazz.getId(), lessonTime)) {
                    expectedLessonCount++;
                }
            }
            for (Subject subject : subjectsGroupedByGrade.get(clazz.getGrade())) {
                scheduledLessonCount += getLessonPerWeek(subject, clazz);
                Teacher teacher = teachers.stream().filter(t -> t.getSubjects().contains(subject.getId())).findAny().orElse(null);
                if(teacher == null) {
                    System.out.println("Teacher conflict");
                    constraintConflicts++;
                }
            }
            if(scheduledLessonCount != expectedLessonCount) {
                System.out.println("Lesson time conflict");
                constraintConflicts++;
            }
            // TODO: validate lessonDistribution and lessonPerWeek
        }
        return constraintConflicts == 0;
    }

    public Schedule startScheduling() throws Exception {
        Schedule schedule = createRandomSchedule();
        double fitness = calculateFitness(schedule);
        double temperature = AlgorithmParams.TEMPERATURE;
        while (fitness < 1) { // TODO: need to add other break conditions
            System.out.println("Temp: " + temperature + ". Fitness: " + fitness);
            for (Schedule neighbor : getNeighbors(schedule)) {
                double neighborFitness = calculateFitness(neighbor);
                calculateFitness(schedule);
                if (neighborFitness >= fitness) {
                    schedule = neighbor;
                    fitness = neighborFitness;
                } else {
                    double loss = Math.abs(fitness - neighborFitness);
                    double acceptanceProb = Math.exp(-loss / temperature);
                    if (rand.nextFloat() < acceptanceProb) {
                        schedule = neighbor;
                        fitness = neighborFitness;
                    }
                }
            }
            temperature *= AlgorithmParams.COOLING_RATE;
        }
        return schedule;
    }

    private Schedule createRandomSchedule() {
        List<Schedule.Lesson> lessons = new ArrayList<>();
        clazzes.forEach(clazz -> {
            lessonTimes.forEach(lessonTime -> {
                if (isLessonOff(clazz.getId(), lessonTime)) {
                    return;
                }
                List<Subject> availableSubjects = subjectsGroupedByGrade.get(clazz.getGrade());
                Subject subject = availableSubjects.get(rand.nextInt(availableSubjects.size()));
                List<Teacher> availableTeachers = teachers.stream().filter(t -> t.getSubjects().contains(subject.getId())).collect(Collectors.toList());
                Teacher teacher = availableTeachers.get(rand.nextInt(availableTeachers.size()));
                Schedule.Lesson lesson = new Schedule.Lesson();
                lesson.setLessonTime(lessonTime);
                lesson.setClazz(clazz);
                lesson.setTeacher(teacher);
                lesson.setSubject(subject);
                lessons.add(lesson);
            });
        });
        return new Schedule(lessons);
    }

    private boolean isLessonOff(Long clazzId, LessonTime lessonTime) {
        return lessonTimeOffByClazzConstraints
                .stream()
                .anyMatch(c -> c.getClazzId().equals(clazzId)
                        && c.getDayOfWeek().equals(lessonTime.getDayOfWeek())
                        && c.getLessonIndex().equals(lessonTime.getLessonIndex())
                );
    }

    private int getLessonPerWeek(Subject subject, Clazz clazz) throws Exception {
        LessonPerWeekByClassConstraint constraint = lessonPerWeekByClassConstraints
                .stream()
                .filter(c -> c.getClazzId().equals(clazz.getId()) && c.getSubjectId().equals(subject.getId())).findFirst()
                .orElse(null);
        if (constraint != null) {
            return constraint.getLessonPerWeek();
        }
        if (subject.getLessonPerWeek() != null) {
            return subject.getLessonPerWeek();
        }
        throw new RuntimeException("Invalid lesson per week constraint for subject id " + subject.getId());
    }

    private double calculateFitness(Schedule schedule) throws Exception {
        int conflicts = 0;
        List<String> conflictReasons = new ArrayList<>();
        List<Schedule.Lesson> lessons = schedule.getLessons();
        Map<Long, Set<Subject>> subjectsGroupByClazz = new HashMap<>();
        Map<Pair<String, Long>, List<Schedule.Lesson>> lessonsGroupedByTimeAndTeacher = new HashMap<>();
        Map<Pair<String, Long>, List<Schedule.Lesson>> lessonsGroupedBySubjectAndClazz = new HashMap<>();
        for (Schedule.Lesson lesson : lessons) {
            Teacher teacher = lesson.getTeacher();
            Clazz clazz = lesson.getClazz();
            Subject subject = lesson.getSubject();

            // group subjects by clazz manually. reuse this loop to decrease time complexity
            Set<Subject> subjectsByClazz = subjectsGroupByClazz.getOrDefault(clazz.getId(), new HashSet<>());
            subjectsByClazz.add(subject);
            subjectsGroupByClazz.put(clazz.getId(), subjectsByClazz);

            // group lessons by subject and clazz manually. reuse this loop to decrease time complexity
            Pair<String, Long> subjectClazzKey = Pair.with(subject.getId(), clazz.getId());
            List<Schedule.Lesson> lessonsBySubjectAndClazz = lessonsGroupedBySubjectAndClazz.getOrDefault(subjectClazzKey, new ArrayList<>());
            lessonsBySubjectAndClazz.add(lesson);
            lessonsGroupedBySubjectAndClazz.put(subjectClazzKey, lessonsBySubjectAndClazz);

            // group lessons by lesson time and teacher manually. reuse this loop to decrease time complexity
            Pair<String, Long> timeTeacherKey = Pair.with(lesson.getLessonTime().toString(), teacher.getId());
            List<Schedule.Lesson> lessonsByTimeAndTeacher = lessonsGroupedByTimeAndTeacher.getOrDefault(timeTeacherKey, new ArrayList<>());
            if (lessonsByTimeAndTeacher.size() > 0) {
                conflicts++;
                conflictReasons.add("Duplicate time for teacher " + teacher.getId());
            }
            lessonsByTimeAndTeacher.add(lesson);
            lessonsGroupedByTimeAndTeacher.put(timeTeacherKey, lessonsByTimeAndTeacher);
        }
        for (Clazz clazz : clazzes) {
            for (Subject subject : subjectsGroupedByGrade.get(clazz.getGrade())) {
                Pair<String, Long> subjectClazzKey = Pair.with(subject.getId(), clazz.getId());
                List<Schedule.Lesson> ls = lessonsGroupedBySubjectAndClazz.get(subjectClazzKey);
                List<Integer> distribution = subject.getLessonDistribution() != null
                        ? new ArrayList<>(subject.getLessonDistribution())
                        : new ArrayList<>();
                if (ls == null) {
                    conflicts++;
                    conflictReasons.add("Subject is not schedule. Clazz: " + clazz.getName() + ". Subject: " + subject.getName());
//                    conflicts++;
//                    conflictReasons.add("Subject is not schedule so it's lesson per week won't satisfy the constraint as well. Clazz: " + clazz.getName() + ". Subject: " + subject.getName());
//                    conflicts += distribution.size(); // if subject is not scheduled, it's distribution won't satisfy the constraint as well
//                    conflictReasons.add("Subject is not schedule so it's distribution won't satisfy the constraint as well. Clazz: " + clazz.getName() + ". Subject: " + subject.getName());
                    continue;
                }
                if (!subject.getGrade().equals(clazz.getGrade())) { // FIXME: some subjects can be applied for all grades
                    conflicts++;
                    conflictReasons.add("Subject grade and clazz grade are not matched. Clazz: " + clazz.getName() + ". Subject: " + subject.getName());
                }
                if (getLessonPerWeek(subject, clazz) != ls.size()) {
                    conflicts++;
                    conflictReasons.add("Subject lesson per week is not match. Clazz: " + clazz.getName() + ". Subject: " + subject.getName());
                }
                int subsequentLessonCount = 1;
                for (int i = 0; i < ls.size(); i++) {
                    Schedule.Lesson lesson = ls.get(i);
                    if (i > 0) {
                        Schedule.Lesson prevLesson = ls.get(i - 1);
                        if (prevLesson.isPreviousLessonOf(lesson)) {
                            subsequentLessonCount++;
                        } else if (subsequentLessonCount > 1) {
                            distribution.remove(Integer.valueOf(subsequentLessonCount));
                            subsequentLessonCount = 1;
                        }
                    }
                }
                distribution.remove(Integer.valueOf(subsequentLessonCount));
                if(distribution.size() > 0) {
                    conflicts += distribution.size();
                    conflictReasons.add("Subject distribution is not satisfied. Clazz: " + clazz.getName() + ". Subject: " + subject.getName());
                }
            }
        }
        return 1.0 / (conflicts + 1);
    }

    private List<Schedule> getNeighbors(Schedule schedule) {
        List<Schedule.Lesson> lessons = schedule.getLessons();
        return IntStream.range(0, AlgorithmParams.NEIGHBOR_SIZE)
                .mapToObj(i -> {
                    Schedule neighbor = new Schedule(new ArrayList<>(lessons));
                    Schedule randomSchedule = createRandomSchedule();
                    IntStream.range(0, AlgorithmParams.NEIGHBOR_DIFFERENCES).forEach(j -> {
                        int randomIndex = rand.nextInt(lessons.size());
                        neighbor.getLessons().set(randomIndex, randomSchedule.getLessons().get(randomIndex));
                    });
                    return neighbor;
                })
                .collect(Collectors.toList());
    }

    public void exportSchedule(Schedule schedule) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("TTB");
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);

        int rowCount = 0;
        int columnCount = 0;
        Row headerRow = sheet.createRow(rowCount++);
        headerRow.createCell(columnCount++);
        headerRow.createCell(columnCount++);
        for (Clazz value : clazzes) {
            headerRow.createCell(columnCount++).setCellValue(value.getName());
        }
        for (LessonTime lt : lessonTimes) {
            if (lt.getLessonIndex() == 1) {
                sheet.createRow(rowCount++);
            }
            Row row = sheet.createRow(rowCount++);
            columnCount = 0;
            row.createCell(columnCount++).setCellValue("Thứ " + (lt.getDayOfWeek() + 2));
            row.createCell(columnCount++).setCellValue("Tiết " + lt.getLessonIndex());
            List<Schedule.Lesson> lessons = schedule.getLessons().stream().filter(l -> l.getLessonTime().equals(lt)).collect(Collectors.toList());
            for (Clazz clazz : clazzes) {
                Schedule.Lesson lesson = lessons.stream().filter(l -> l.getClazz().getId().equals(clazz.getId())).findFirst().orElse(null);
                if (lesson != null) {
                    Cell cell = row.createCell(columnCount++);
                    cell.setCellStyle(wrapStyle);
                    cell.setCellValue(lesson.getSubject().getName() + "\n(" + lesson.getTeacher().getName() + ")");
                } else {
                    columnCount++;

                }
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream("TTB.xlsx")) {
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
