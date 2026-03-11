package com.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JournalVO {

    private Long id;
    private Long applicationId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String title;
    private String content;
    private String journalType;
    private LocalDate journalDate;
    private Integer weekNo;
    private String teacherComment;
    private BigDecimal score;
    private LocalDateTime scoredAt;
    private LocalDateTime createTime;
}
