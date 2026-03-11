package com.dto;

import com.common.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalDTO {

    @NotNull(message = "审批状态不能为空")
    private ApprovalStatus status;

    private String comment;
}
