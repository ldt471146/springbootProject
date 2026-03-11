package com.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeVO {

    private Long id;
    private Long applicationId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private BigDecimal journalScore;
    private BigDecimal reportScore;
    private BigDecimal finalScore;
    private String teacherComment;
    private String adminStatus;
    private String adminComment;
    private LocalDateTime confirmedAt;
    private LocalDateTime createTime;
}
