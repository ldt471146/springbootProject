package com.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationVO {

    private Long id;
    private Long projectId;
    private String projectTitle;
    private String projectStatus;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String teacherName;
    private String status;
    private String phase;
    private String applyReason;
    private String reviewComment;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createTime;
}
