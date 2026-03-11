package com.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReportVO {

    private Long id;
    private Long applicationId;
    private String projectTitle;
    private Long studentId;
    private String studentName;
    private String title;
    private String summary;
    private String fileUrl;
    private String semester;
    private String creditType;
    private String status;
    private String teacherComment;
    private BigDecimal teacherScore;
    private Integer isExcellent;
    private String aiFlag;
    private LocalDateTime reviewedAt;
    private LocalDateTime createTime;
}
