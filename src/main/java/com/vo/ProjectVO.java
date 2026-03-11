package com.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectVO {

    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String company;
    private String location;
    private Integer maxStudents;
    private String collegeLimit;
    private String gradeLimit;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate enrollDeadline;
    private String semester;
    private String creditType;
    private String status;
    private Integer participantCount;
    private LocalDateTime createTime;
}
