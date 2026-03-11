package com.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_attachment")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String refType;
    private Long refId;
    private String originalName;
    private String storedName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private Long createBy;
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
