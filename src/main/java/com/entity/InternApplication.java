package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("intern_application")
public class InternApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long studentId;
    private String applyReason;
    private String status;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String phase;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
