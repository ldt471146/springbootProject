package com.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationDTO {

    @NotNull(message = "项目不能为空")
    private Long projectId;
    private String applyReason;
}
