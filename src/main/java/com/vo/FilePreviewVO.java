package com.vo;

import lombok.Data;

@Data
public class FilePreviewVO {

    private String mode;
    private String fileName;
    private String contentType;
    private String htmlContent;
    private String message;
}
