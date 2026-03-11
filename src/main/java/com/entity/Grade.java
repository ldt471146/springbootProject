package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("intern_grade")
public class Grade {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Long studentId;
    private Long teacherId;
    private BigDecimal journalScore;
    private BigDecimal reportScore;
    private BigDecimal finalScore;
    private String teacherComment;
    private String adminStatus;
    private String adminComment;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
