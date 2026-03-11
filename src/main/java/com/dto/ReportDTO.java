package com.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDTO {

    @NotNull(message = "申请不能为空")
    private Long applicationId;
    @NotBlank(message = "报告标题不能为空")
    private String title;
    private String summary;
    @NotBlank(message = "文件地址不能为空")
    private String fileUrl;
    private String semester;
    private String creditType;
}
