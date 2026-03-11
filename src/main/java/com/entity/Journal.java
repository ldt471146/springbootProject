package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("intern_journal")
public class Journal {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Long studentId;
    private String title;
    private String content;
    private String journalType;
    private LocalDate journalDate;
    private Integer weekNo;
    private String teacherComment;
    private BigDecimal score;
    private LocalDateTime scoredAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
