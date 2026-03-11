package com.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeDTO {

    @NotNull(message = "申请不能为空")
    private Long applicationId;
    @NotNull(message = "日志成绩不能为空")
    @DecimalMin(value = "0", message = "日志成绩不能小于0")
    @DecimalMax(value = "100", message = "日志成绩不能大于100")
    private BigDecimal journalScore;
    @NotNull(message = "报告成绩不能为空")
    @DecimalMin(value = "0", message = "报告成绩不能小于0")
    @DecimalMax(value = "100", message = "报告成绩不能大于100")
    private BigDecimal reportScore;
    private String teacherComment;
}
