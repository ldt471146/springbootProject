package com.dto;

import com.common.enums.ReportStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportReviewDTO {

    @NotNull(message = "状态不能为空")
    private ReportStatus status;
    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "100", message = "评分不能大于100")
    private BigDecimal score;
    private String comment;
    private Boolean isExcellent;
}
