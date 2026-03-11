package com.dto;

import com.common.enums.AdminApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeConfirmDTO {

    @NotNull(message = "终审状态不能为空")
    private AdminApprovalStatus adminStatus;
    private String adminComment;
}
