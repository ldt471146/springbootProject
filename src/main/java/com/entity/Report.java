package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("intern_report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Long studentId;
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
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
