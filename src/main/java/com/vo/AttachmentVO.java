package com.vo;

import lombok.Data;

@Data
public class AttachmentVO {

    private Long id;
    private String originalName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
}
